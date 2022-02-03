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

            var startupProjects = new LinkedList<IProject>(projectsWithInstalledNugetPacks);

            foreach (var project in projectsWithInstalledNugetPacks)
            {
                FillStartupProjectsList(startupProjects, project);
            }

            return startupProjects;
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

        private static void FillStartupProjectsList(LinkedList<IProject> startupProjects, IProject project)
        {
            var targetFramework = project.TargetFrameworkIds;

            foreach (var frameworkId in targetFramework)
            {
                var referencingProjectsEx = project
                    .GetReferencingProjectsEx(frameworkId)
                    .ToList();

                referencingProjectsEx.ForEach(x =>
                {
                    if (!startupProjects.Contains(x.Value))
                    {
                        startupProjects.AddLast(x.Value);
                    }
                });
            }
        }
    }
}