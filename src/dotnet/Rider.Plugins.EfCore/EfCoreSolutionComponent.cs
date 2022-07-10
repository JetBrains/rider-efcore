using System.Collections.Generic;
using System.Linq;
using JetBrains.Core;
using JetBrains.Diagnostics;
using JetBrains.Lifetimes;
using JetBrains.ProjectModel;
using JetBrains.ProjectModel.NuGet.DotNetTools;
using JetBrains.ProjectModel.Tasks;
using JetBrains.Rd.Tasks;
using JetBrains.RdBackend.Common.Features;
using JetBrains.ReSharper.Psi;
using JetBrains.ReSharper.Psi.Modules;
using JetBrains.ReSharper.Psi.Util;
using JetBrains.ReSharper.Resources.Shell;
using JetBrains.RiderTutorials.Utils;
using JetBrains.Threading;
using JetBrains.Util;
using Rider.Plugins.EfCore.Compatibility;
using Rider.Plugins.EfCore.Exceptions;
using Rider.Plugins.EfCore.Migrations;
using Rider.Plugins.EfCore.Rd;

namespace Rider.Plugins.EfCore
{
    [SolutionComponent]
    public class EfCoreSolutionComponent
    {
        private readonly Lifetime _lifetime;
        private readonly ISolution _solution;
        private readonly ILogger _logger;

        private readonly JetFastSemiReenterableRWLock _lock = new JetFastSemiReenterableRWLock();
        private readonly RiderEfCoreModel _efCoreModel;

        private bool _toolsNotInstalledNotified = false;

        public EfCoreSolutionComponent(
            Lifetime lifetime,
            ISolution solution,
            NuGetDotnetToolsTracker dotnetToolsTracker,
            SolutionStructureChangedListener solutionStructureChangedListener,
            NugetDependenciesListener nuGetPackageReferenceTracker,
            ISolutionLoadTasksScheduler solutionLoadTasksScheduler,
            ILogger logger)
        {
            _lifetime = lifetime;
            _solution = solution;
            _logger = logger;

            _efCoreModel = solution.GetProtocolSolution().GetRiderEfCoreModel();

            _efCoreModel.HasAvailableMigrations.Set(HasAvailableMigrations);
            _efCoreModel.GetAvailableMigrations.Set(GetAvailableMigrations);
            _efCoreModel.GetAvailableDbContexts.Set(GetAvailableDbContexts);

            dotnetToolsTracker.DotNetToolCache.Change.Advise(lifetime, args =>
            {
                if (!args.HasNew || args.New is null) return;
                using var _ = _lock.UsingWriteLock();

                var cache = args.New;
                InvalidateEfToolsDefinition(cache);
            });

            nuGetPackageReferenceTracker.ProjectsUpdated += InvalidateStartupProjects;

            solutionLoadTasksScheduler.EnqueueTask(
                new SolutionLoadTask(
                    $"{nameof(EfCoreSolutionComponent)}.{nameof(InvalidateProjects)}",
                    SolutionLoadTaskKinds.Done,
                    () =>
                    {
                        InvalidateProjects();

                        var efToolsDefinitionValue = _efCoreModel.EfToolsDefinition.Value;
                        if (efToolsDefinitionValue != null)
                        {
                            CheckToolsInstalled(efToolsDefinitionValue);
                        }

                        _efCoreModel.EfToolsDefinition.Advise(lifetime, CheckToolsInstalled);
                        solutionStructureChangedListener.SolutionChanged += InvalidateProjects;
                    }
                ));
        }

        private void CheckToolsInstalled(EfToolDefinition efToolsDefinition)
        {
            var startupProjects = _efCoreModel.AvailableStartupProjects.Value;

            if (!_toolsNotInstalledNotified && startupProjects.Count > 0 && efToolsDefinition.ToolKind == ToolKind.None)
            {
                _toolsNotInstalledNotified = true;
                _efCoreModel.OnMissingEfCoreToolsDetected.Start(_lifetime, Unit.Instance);
            }
        }

        private void InvalidateEfToolsDefinition(DotNetToolCache cache)
        {
            var allLocalTools = cache.ToolLocalCache.GetAllLocalTools();
            if (allLocalTools is null) return;

            var dotnetEfLocalTool = allLocalTools.FirstOrDefault(tool => tool.PackageId == KnownDotnetTools.EfCoreTools);

            var toolKind = ToolKind.None;
            var version = string.Empty;
            if (dotnetEfLocalTool is {})
            {
                toolKind = ToolKind.Local;
                version = dotnetEfLocalTool.Version;
            }
            else
            {
                var dotnetEfGlobalTool = cache.ToolGlobalCache.GetGlobalTool(KnownDotnetTools.EfCoreTools);
                if (dotnetEfGlobalTool is { Count: 1 })
                {
                    toolKind = ToolKind.Global;
                    version = dotnetEfGlobalTool[0].Version.ToString();
                }
            }

            _efCoreModel.EfToolsDefinition.Value = new EfToolDefinition(version, toolKind);
        }

        private void InvalidateProjects()
        {
            InvalidateStartupProjects();
            InvalidateMigrationsProjects();
        }

        private void InvalidateStartupProjects()
        {
            using var cookie = ReadLockCookie.Create();

            var allProjectNames = _solution
                .GetSupportedStartupProjects(_logger)
                .Select(project => new StartupProjectInfo(
                    project.Guid,
                    project.Name,
                    project.ProjectFileLocation.FullPath,
                    project.TargetFrameworkIds
                        .Where(frameworkId => !frameworkId.IsNetStandard)
                        .Select(frameworkId => frameworkId.MapTargetFrameworkId())
                        .ToList(),
                    project.GetDefaultNamespace() ?? string.Empty))
                .ToList();

            _efCoreModel.AvailableStartupProjects.Value = allProjectNames;

            _logger.Log(LoggingLevel.WARN, "[EF Core]: Startup projects invalidated:" +
                                           $"\n\t{string.Join("\n\t", _efCoreModel.AvailableStartupProjects.Value.Select(project => project.Name))}");
        }

        private void InvalidateMigrationsProjects()
        {
            using var cookie = ReadLockCookie.Create();

            var allProjectNames = _solution
                .GetSupportedMigrationProjects()
                .Select(project => new MigrationsProjectInfo(
                    project.Guid,
                    project.Name,
                    project.ProjectFileLocation.FullPath,
                    project.GetDefaultNamespace() ?? string.Empty))
                .ToList();

            _efCoreModel.AvailableMigrationProjects.Value = allProjectNames;

            _logger.Log(LoggingLevel.WARN, "[EF Core]: Migration projects invalidated:" +
                                           $"\n\t{string.Join("\n\t", _efCoreModel.AvailableMigrationProjects.Value.Select(project => project.Name))}");
        }

        //
        // Calls implementations

        private RdTask<bool> HasAvailableMigrations(Lifetime lifetime, MigrationsIdentity identity)
        {
            using (CompilationContextCookie.GetExplicitUniversalContextIfNotSet())
            using (ReadLockCookie.Create())
            {
                var project = _solution.GetProjectByName(identity.ProjectName);
                if (project is null)
                {
                    return RdTask<bool>.Faulted(new ProjectNotFoundException(identity.ProjectName));
                }

                var projectHasMigrations = project.GetPsiModules()
                    ?.SelectMany(module => module.FindInheritorsOf(project, EfCoreKnownTypeNames.MigrationBaseClass))
                    .Select(cl => cl.ToMigrationInfo())
                    .Any(migrationInfo =>
                        migrationInfo != null && migrationInfo.DbContextClassFullName == identity.DbContextClassFullName);

                return RdTask<bool>.Successful(projectHasMigrations ?? false);
            }
        }

        private RdTask<List<MigrationInfo>> GetAvailableMigrations(Lifetime lifetime, MigrationsIdentity identity)
        {
            using (CompilationContextCookie.GetExplicitUniversalContextIfNotSet())
            using (ReadLockCookie.Create())
            {
                var project = _solution.GetProjectByName(identity.ProjectName);
                if (project is null)
                {
                    return RdTask<List<MigrationInfo>>.Faulted(new ProjectNotFoundException(identity.ProjectName));
                }

                var foundMigrations = project
                    .GetPsiModules()
                    .SelectMany(module => module.FindInheritorsOf(project, EfCoreKnownTypeNames.MigrationBaseClass))
                    .Distinct(migrationClass => migrationClass.GetFullClrName()) // To get around of multiple modules (multiple target frameworks)
                    .Select(migrationClass => migrationClass.ToMigrationInfo())
                    .Where(m => m.DbContextClassFullName == identity.DbContextClassFullName)
                    .ToList();

                return RdTask<List<MigrationInfo>>.Successful(foundMigrations);
            }
        }

        private RdTask<List<DbContextInfo>> GetAvailableDbContexts(Lifetime lifetime, string projectName)
        {
            using (CompilationContextCookie.GetExplicitUniversalContextIfNotSet())
            using (ReadLockCookie.Create())
            {
                var project = _solution.GetProjectByName(projectName);
                if (project is null)
                {
                    return RdTask<List<DbContextInfo>>.Faulted(new ProjectNotFoundException(projectName));
                }

                var foundDbContexts = project
                    .GetPsiModules()
                    .SelectMany(module => module.FindInheritorsOf(project, EfCoreKnownTypeNames.DbContextBaseClass))
                    .Distinct(dbContextClass => dbContextClass.GetFullClrName()) // To get around of multiple modules (multiple target frameworks)
                    .Select(dbContextClass =>
                        new DbContextInfo(dbContextClass.ShortName, dbContextClass.GetFullClrName()))
                    .ToList();

                return RdTask<List<DbContextInfo>>.Successful(foundDbContexts);
            }
        }
    }
}