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
            var projectsWithInstalledNugetPacks = solution
                .GetSupportedDotnetProjects(IsStartupProjectSupported)
                .Where(StartupProjectPackagesInstalled)
                .ToList();

            var projectsLinkedList = new LinkedList<IProject>(projectsWithInstalledNugetPacks);

            var referencingProjects = projectsWithInstalledNugetPacks.SelectMany(GetReferencingProjects);

            foreach (var project in referencingProjects)
            {
                if (project.IsNetStandard21())
                {
                    continue;
                }

                if (projectsLinkedList.Contains(project))
                {
                    continue;
                }

                projectsLinkedList.AddLast(project);
            }

            return projectsLinkedList;
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

        private static IEnumerable<IProject> GetReferencingProjects(IProject project)
        {
            var linkedList = new LinkedList<IProject>();

            var targetFramework = project.TargetFrameworkIds;

            foreach (var frameworkId in targetFramework)
            {
                var referencingProjects = project
                    .GetReferencingProjectsEx(frameworkId)
                    .Select(x => x.Value)
                    .ToList();

                referencingProjects.ForEach(x => linkedList.AddLast(x));
            }

            var result = linkedList.Distinct();

            return result;
        }

        private static bool IsNetStandard21(this IProject project)
        {
            var containsNetStandardTarget = project.TargetFrameworkIds
                .Any(x => x.UniqueString == EfCoreSupportedTarget.NetStandard21);

            return containsNetStandardTarget;
        }
    }
}