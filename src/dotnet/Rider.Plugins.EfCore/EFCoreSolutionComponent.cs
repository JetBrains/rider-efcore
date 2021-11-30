using System.Collections.Generic;
using System.Linq;
using JetBrains.Core;
using JetBrains.Lifetimes;
using JetBrains.ProjectModel;
using JetBrains.Rd.Tasks;
using JetBrains.RdBackend.Common.Features;
using JetBrains.ReSharper.Psi;
using JetBrains.ReSharper.Psi.Modules;
using JetBrains.ReSharper.Resources.Shell;
using JetBrains.RiderTutorials.Utils;
using JetBrains.Util;
using Rider.Plugins.EfCore.Extensions;
using Rider.Plugins.EfCore.Rd;

namespace Rider.Plugins.EfCore
{
    [SolutionComponent]
    public class EfCoreSolutionComponent
    {
        private readonly ISolution _solution;

        public EfCoreSolutionComponent(ISolution solution, ILogger logger)
        {
            _solution = solution;

            var riderProjectOutputModel = solution.GetProtocolSolution().GetRiderEfCoreModel();

            riderProjectOutputModel.GetAvailableMigrationsProjects.Set(GetAvailableMigrationsProjects);
            riderProjectOutputModel.GetAvailableStartupProjects.Set(GetAvailableStartupProjects);
            riderProjectOutputModel.HasAvailableMigrations.Set(HasAvailableMigrations);
            riderProjectOutputModel.GetAvailableMigrations.Set(GetAvailableMigrations);
            riderProjectOutputModel.GetAvailableDbContexts.Set(GetAvailableDbContexts);
        }

        private RdTask<List<MigrationsProjectInfo>> GetAvailableMigrationsProjects(Lifetime lifetime, Unit _)
        {
            using (ReadLockCookie.Create())
            {
                var allProjectNames = EfCoreHelper.GetSupportedMigrationProjects(_solution)
                    .Select(project => new MigrationsProjectInfo(project.Name, project.ProjectFileLocation.FullPath))
                    .ToList();

                return RdTask<List<MigrationsProjectInfo>>.Successful(allProjectNames);
            }
        }

        private RdTask<List<StartupProjectInfo>> GetAvailableStartupProjects(Lifetime lifetime, Unit _)
        {
            using (ReadLockCookie.Create())
            {
                var allProjectNames = EfCoreHelper.GetSupportedStartupProjects(_solution)
                    .Select(project => new StartupProjectInfo(
                        project.Name,
                        project.ProjectFileLocation.FullPath,
                        project.TargetFrameworkIds.Select(fr => fr.PresentableString).ToList()))
                    .ToList();

                return RdTask<List<StartupProjectInfo>>.Successful(allProjectNames);
            }
        }

        private RdTask<bool> HasAvailableMigrations(Lifetime lifetime, string projectName)
        {
            using (ReadLockCookie.Create())
            {
                var project = _solution.GetProjectByName(projectName);

                var projectHasMigrations = project
                    ?.GetPsiModules()
                    ?.SelectMany(module => module.FindInheritorsOf(project, EfCoreKnownTypeNames.MigrationBaseClass))
                    .Any();

                return RdTask<bool>.Successful(projectHasMigrations ?? false);
            }
        }

        private RdTask<List<MigrationInfo>> GetAvailableMigrations(Lifetime lifetime, string projectName)
        {
            using (CompilationContextCookie.GetExplicitUniversalContextIfNotSet())
            using (ReadLockCookie.Create())
            {
                var project = _solution.GetProjectByName(projectName);

                // TODO: Refactor to simplify
                var foundMigrations = project
                    .GetPsiModules()
                    .SelectMany(module => module.FindInheritorsOf(project, EfCoreKnownTypeNames.MigrationBaseClass))
                    .Select(cl => (className: cl.ShortName, attribute: cl.GetAttributeInstance("MigrationAttribute")))
                    .Where(items => items.attribute != null)
                    .Select(items => (shortName: items.className, longName: items.attribute.PositionParameter(0).ConstantValue.Value as string))
                    .Select(migration => new MigrationInfo(migration.shortName, migration.longName))
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

                var foundDbContexts = project
                    .GetPsiModules()
                    .SelectMany(module => module.FindInheritorsOf(project, EfCoreKnownTypeNames.DbContextBaseClass))
                    .Select(dbContextClass => new DbContextInfo(dbContextClass.ShortName, dbContextClass.GetFullClrName()))
                    .ToList();

                return RdTask<List<DbContextInfo>>.Successful(foundDbContexts);
            }
        }
    }
}