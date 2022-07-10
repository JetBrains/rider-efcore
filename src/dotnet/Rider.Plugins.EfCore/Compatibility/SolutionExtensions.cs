using System.Collections.Generic;
using System.Linq;
using JetBrains.Diagnostics;
using JetBrains.ProjectModel;
using JetBrains.Util;

namespace Rider.Plugins.EfCore.Compatibility
{
    public static class SolutionExtensions
    {
        public static IEnumerable<IProject> GetSupportedMigrationProjects(this ISolution solution)
        {
            var supportedMigrationProjects = solution
                .GetSupportedDotnetProjects(tfId => tfId.IsSupportedInMigrationsProject())
                .Where(project => project.ProjectFileLocation.ExtensionNoDot == "csproj");

            return supportedMigrationProjects;
        }

        public static IEnumerable<IProject> GetSupportedStartupProjects(this ISolution solution, ILogger logger)
        {
            var projectsWithNugetPacks = solution.GetAllProjects()
                .Where(StartupProjectPackagesInstalled)
                .ToList();

            var referencingProjects = projectsWithNugetPacks.SelectMany(GetReferencingProjects).ToList();

            logger.Log(LoggingLevel.TRACE, $"[EF Core]: Projects with nuget packs: {projectsWithNugetPacks.Count}, referencing projects: {referencingProjects.Count}");

            var result = projectsWithNugetPacks
                .Concat(referencingProjects)
                .Where(project => project.TargetFrameworkIds.Any(tfId => tfId.IsSupportedInStartupProject()))
                .Distinct();

            return result;
        }

        private static bool StartupProjectPackagesInstalled(IProject project) =>
            project.GetInstalledPackage(EfCoreRequiredPackages.EfCoreToolsNugetId) != default
            || project.GetInstalledPackage(EfCoreRequiredPackages.EfCoreDesignNugetId) != default;

        private static IEnumerable<IProject> GetReferencingProjects(this IProject project) =>
            project.TargetFrameworkIds
                .SelectMany(x => project.GetReferencingProjectsEx(x))
                .Select(x => x.Value)
                .ToList();
    }
}