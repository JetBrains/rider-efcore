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
            var supportedStartupProjects = solution
                .GetSupportedDotnetProjects(IsStartupProjectSupported)
                .Where(IsRequiredNugetPackageInstalled);

            return supportedStartupProjects;
        }

        private static IEnumerable<IProject> GetSupportedDotnetProjects(this IProjectCollection solution,
            Func<TargetFrameworkId, bool> condition)
        {
            var supportedDotnetProjects = solution.GetAllProjects()
                .Where(project => project.TargetFrameworkIds.Any(condition));

            return supportedDotnetProjects;
        }

        private static bool IsMigrationProjectSupported(TargetFrameworkId targetFrameworkId) =>
            targetFrameworkId.UniqueString == EfCoreSupportedTarget.Net5Target
            || targetFrameworkId.UniqueString == EfCoreSupportedTarget.Net6Target
            || targetFrameworkId.UniqueString == EfCoreSupportedTarget.NetCore31Target
            || targetFrameworkId.UniqueString == EfCoreSupportedTarget.NetStandard21Target;

        private static bool IsStartupProjectSupported(TargetFrameworkId targetFrameworkId) =>
            targetFrameworkId.UniqueString == EfCoreSupportedTarget.Net5Target
            || targetFrameworkId.UniqueString == EfCoreSupportedTarget.Net6Target
            || targetFrameworkId.UniqueString == EfCoreSupportedTarget.NetCore31Target;

        private static bool IsRequiredNugetPackageInstalled(IProject project) =>
            project.GetInstalledPackage(EfCoreRequiredPackages.EfCoreToolsNugetId) != default
            || project.GetInstalledPackage(EfCoreRequiredPackages.EfCoreDesignNugetId) != default;
    }
}