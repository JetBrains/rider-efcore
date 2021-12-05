using System.Collections.Generic;
using System.Linq;
using JetBrains.ProjectModel;
using JetBrains.ProjectModel.Resources;
using JetBrains.Util.Dotnet.TargetFrameworkIds;

namespace Rider.Plugins.EfCore
{
    public static class EfCoreHelper
    {
        private static readonly string EfCoreDesignNugetId = "Microsoft.EntityFrameworkCore.Design";
        private static readonly string EfCoreToolsNugetId = "Microsoft.EntityFrameworkCore.Tools";

        public static IEnumerable<IProject> GetSupportedMigrationProjects(ISolution solution) =>
            GetSupportedDotnetProjects(solution)
                .Where(project => project.ProjectFileLocation.ExtensionNoDot == "csproj");

        public static IEnumerable<IProject> GetSupportedStartupProjects(ISolution solution) =>
            GetSupportedDotnetProjects(solution)
                .Where(project => project.GetInstalledPackage(EfCoreToolsNugetId) != default
                                  || project.GetInstalledPackage(EfCoreDesignNugetId) != default);

        private static IEnumerable<IProject> GetSupportedDotnetProjects(ISolution solution) =>
            solution
                .GetAllProjects()
                .Where(project => project.TargetFrameworkIds
                    .Any(IsTargetFrameworkSupported));

        private static bool IsTargetFrameworkSupported(TargetFrameworkId targetFrameworkId) =>
            targetFrameworkId.UniqueString == "net5.0"
            || targetFrameworkId.UniqueString == "net6.0";
    }
}