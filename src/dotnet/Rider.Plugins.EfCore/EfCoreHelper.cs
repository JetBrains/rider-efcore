using System;
using System.Collections.Generic;
using System.Linq;
using JetBrains.ProjectModel;
using JetBrains.Util.Dotnet.TargetFrameworkIds;

namespace Rider.Plugins.EfCore
{
    public static class EfCoreHelper
    {
        public static IEnumerable<IProject> GetSupportedMigrationProjects(ISolution solution)
        {
            var supportedMigrationProjects = solution
                .GetSupportedDotnetProjects(IsMigrationProjectSupported)
                .Where(project => project.ProjectFileLocation.ExtensionNoDot == "csproj");

            return supportedMigrationProjects;
        }

        public static IEnumerable<IProject> GetSupportedStartupProjects(ISolution solution)
        {
            var projectsWithNugetPacks = solution
                .GetSupportedDotnetProjects(IsStartupProjectSupported)
                .Where(StartupProjectPackagesInstalled)
                .ToList();

            var referencingProjects = projectsWithNugetPacks
                .SelectMany(p => p.GetReferencingProjects(projectsWithNugetPacks));

            var result = referencingProjects.Distinct();

            return result;
        }

        private static IEnumerable<IProject> GetSupportedDotnetProjects(this IProjectCollection solution,
            Func<TargetFrameworkId, bool> condition)
        {
            var supportedDotnetProjects = solution.GetAllProjects()
                .Where(project => project.TargetFrameworkIds.Any(condition));

            return supportedDotnetProjects;
        }

        private static bool IsMigrationProjectSupported(TargetFrameworkId targetFrameworkId) =>
            targetFrameworkId.UniqueString == EfCoreSupportedTarget.Net5
            || targetFrameworkId.UniqueString == EfCoreSupportedTarget.Net6
            || targetFrameworkId.UniqueString == EfCoreSupportedTarget.NetCore31
            || targetFrameworkId.UniqueString == EfCoreSupportedTarget.NetStandard21;

        private static bool IsStartupProjectSupported(TargetFrameworkId targetFrameworkId) =>
            targetFrameworkId.UniqueString == EfCoreSupportedTarget.Net5
            || targetFrameworkId.UniqueString == EfCoreSupportedTarget.Net6
            || targetFrameworkId.UniqueString == EfCoreSupportedTarget.NetCore31;

        private static bool StartupProjectPackagesInstalled(IProject project) =>
            project.GetInstalledPackage(EfCoreRequiredPackages.EfCoreToolsNugetId) != default
            || project.GetInstalledPackage(EfCoreRequiredPackages.EfCoreDesignNugetId) != default;

        private static IEnumerable<IProject> GetReferencingProjects(this IProject project,
            IEnumerable<IProject> list)
        {
            var linkedList = new LinkedList<IProject>(list);

            var targetFramework = project.TargetFrameworkIds;

            foreach (var frameworkId in targetFramework)
            {
                var referencingProjects = project
                    .GetReferencingProjectsEx(frameworkId)
                    .Select(pair => pair.Value)
                    .Where(proj => !proj.IsNetStandard())
                    .ToList();

                referencingProjects.ForEach(x => linkedList.AddLast(x));
            }

            var result = linkedList.Distinct();

            return result;
        }

        private static bool IsNetStandard(this IProject project)
        {
            var containsNetStandardTarget = project.TargetFrameworkIds
                .Any(x => x.IsNetStandard);

            return containsNetStandardTarget;
        }
    }
}