using System.Collections.Generic;
using System.Linq;
using JetBrains.ProjectModel;
using JetBrains.ProjectModel.Search;
using JetBrains.ReSharper.Resources.Shell;
using JetBrains.Util.Dotnet.TargetFrameworkIds;

namespace ReSharperPlugin.RiderEfCore
{
    public static class EfCoreHelper
    {
        private static readonly string EfCoreToolsNugetId = "Microsoft.EntityFrameworkCore.Design";

        public static IEnumerable<IProject> GetSupportedMigrationProjects(ISolution solution) =>
            GetSupportedDotnetProjects(solution);

        public static IEnumerable<IProject> GetSupportedStartupProjects(ISolution solution) =>
            GetSupportedDotnetProjects(solution)
                .Where(project => project.GetInstalledPackage(EfCoreToolsNugetId) != default);

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