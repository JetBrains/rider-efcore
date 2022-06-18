using System.Collections.Generic;
using System.Linq;
using JetBrains.Lifetimes;
using JetBrains.ProjectModel;
using JetBrains.ProjectModel.NuGet.DotNetTools;
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
        private readonly ISolution _solution;

        private readonly JetFastSemiReenterableRWLock _lock = new JetFastSemiReenterableRWLock();
        private readonly RiderEfCoreModel _efCoreModel;

        public EfCoreSolutionComponent(
            Lifetime lifetime,
            ISolution solution,
            NuGetDotnetToolsTracker dotnetToolsTracker,
            SolutionStructureChangedListener solutionStructureChangedListener,
            ILogger logger)
        {
            _solution = solution;

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

            solutionStructureChangedListener.SolutionChanged += InvalidateProjects;
            InvalidateProjects();
        }

        private void InvalidateEfToolsDefinition(DotNetToolCache cache)
        {
            var allLocalTools = cache.ToolLocalCache.GetAllLocalTools();
            if (allLocalTools is null) return;

            var dotnetEfLocalTool = allLocalTools.FirstOrDefault(tool => tool.PackageId == "dotnet-ef");

            var toolKind = ToolKind.None;
            var version = string.Empty;
            if (dotnetEfLocalTool is {})
            {
                toolKind = ToolKind.Local;
                version = dotnetEfLocalTool.Version;
            }
            else
            {
                var dotnetEfGlobalTool = cache.ToolGlobalCache.GetGlobalTool("dotnet-ef");
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
                .GetSupportedStartupProjects()
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