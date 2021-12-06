using System.Collections.Generic;
using System.Linq;
using JetBrains.ProjectModel;
using JetBrains.Util.Dotnet.TargetFrameworkIds;

namespace Rider.Plugins.EfCore
{
    public static class EfCoreHelper
    {
        private const string EfCoreDesignNugetId = "Microsoft.EntityFrameworkCore.Design";
        private const string EfCoreToolsNugetId = "Microsoft.EntityFrameworkCore.Tools";

        private const string Net5Target = "net5.0";
        private const string Net6Target = "net6.0";
        private const string NetCore31Target = ".NETCoreApp,Version=v3.1";
        private const string NetStandard21Target = ".NETStandard,Version=v2.1";

        public static IEnumerable<IProject> GetSupportedMigrationProjects(ISolution solution)
        {
            var supportedMigrationProjects = GetSupportedDotnetProjects(solution)
                .Where(project => project.ProjectFileLocation.ExtensionNoDot == "csproj");

            return supportedMigrationProjects;
        }

        public static IEnumerable<IProject> GetSupportedStartupProjects(ISolution solution)
        {
            var supportedStartupProjects = GetSupportedDotnetProjects(solution)
                .Where(project => project.TargetFrameworkIds.All(IsNotNetStandard))
                .Where(project => project.GetInstalledPackage(EfCoreToolsNugetId) != default
                                  || project.GetInstalledPackage(EfCoreDesignNugetId) != default);

            return supportedStartupProjects;
        }

        private static IEnumerable<IProject> GetSupportedDotnetProjects(ISolution solution)
        {
            var supportedDotnetProjects = solution.GetAllProjects()
                .Where(project => project.TargetFrameworkIds.Any(IsTargetFrameworkSupported));

            return supportedDotnetProjects;
        }

        private static bool IsTargetFrameworkSupported(TargetFrameworkId targetFrameworkId) =>
            targetFrameworkId.UniqueString == Net5Target
            || targetFrameworkId.UniqueString == Net6Target
            || targetFrameworkId.UniqueString == NetCore31Target
            || targetFrameworkId.UniqueString == NetStandard21Target;

        private static bool IsNotNetStandard(TargetFrameworkId targetFrameworkId) =>
            targetFrameworkId.UniqueString != NetStandard21Target;
    }
}