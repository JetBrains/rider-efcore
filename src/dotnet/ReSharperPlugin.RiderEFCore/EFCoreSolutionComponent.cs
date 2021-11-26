using System;
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
using JetBrains.Rider.Model;
using JetBrains.Util;
using ReSharperPlugin.RiderEfCore.Extensions;

namespace ReSharperPlugin.RiderEfCore
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
            riderProjectOutputModel.GetAvailableMigrations.Set(GetAvailableMigrations);
        }

        private RdTask<List<ProjectInfo>> GetAvailableMigrationsProjects(Lifetime lifetime, Unit _)
        {
            using (ReadLockCookie.Create())
            {
                var allProjectNames = EfCoreHelper.GetSupportedMigrationProjects(_solution)
                    .Select(project => new ProjectInfo(project.Name, project.ProjectFileLocation.FullPath))
                    .ToList();

                return RdTask<List<ProjectInfo>>.Successful(allProjectNames);
            }
        }

        private RdTask<List<ProjectInfo>> GetAvailableStartupProjects(Lifetime lifetime, Unit _)
        {
            using (ReadLockCookie.Create())
            {
                var allProjectNames = EfCoreHelper.GetSupportedStartupProjects(_solution)
                    .Select(project => new ProjectInfo(project.Name, project.ProjectFileLocation.FullPath))
                    .ToList();

                return RdTask<List<ProjectInfo>>.Successful(allProjectNames);
            }
        }

        private RdTask<List<string>> GetAvailableMigrations(Lifetime lifetime, string projectName)
        {
            // using (CompilationContextCookie.GetOrCreate(new PsiModuleResolveContext()))
            using (CompilationContextCookie.GetExplicitUniversalContextIfNotSet())
            using (ReadLockCookie.Create())
            {
                var project = _solution.GetProjectByName(projectName);

                // TODO: Refactor to simplify
                var foundMigrations = project
                    .GetPsiModules()
                    .SelectMany(module => module.FindInheritorsOf(project, EfCoreKnownTypeNames.MigrationBaseClass))
                    .Select(occurence => occurence.GetAttributeInstances(AttributesSource.All)
                        .SingleOrDefault(attribute => attribute.GetAttributeShortName() == "MigrationAttribute"))
                    .Where(a => a != null)
                    .Select(a => a.PositionParameter(0).ConstantValue.Value as string)
                    .ToList();

                return RdTask<List<string>>.Successful(foundMigrations);
            }
        }
    }
}