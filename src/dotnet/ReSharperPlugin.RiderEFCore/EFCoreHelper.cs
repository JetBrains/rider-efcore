using System.Collections.Generic;
using System.Linq;
using JetBrains.ProjectModel;
using JetBrains.Util.Dotnet.TargetFrameworkIds;

namespace ReSharperPlugin.RiderEfCore
{
    public static class EfCoreHelper
    {
        public static IEnumerable<IProject> GetSupportedMigrationProjects(ISolution solution)
        {
            return GetSupportedDotnetProjects(solution);
        }

        public static IEnumerable<IProject> GetSupportedStartupProjects(ISolution solution)
        {
            return GetSupportedDotnetProjects(solution);
        }

        private static IEnumerable<IProject> GetSupportedDotnetProjects(ISolution solution) =>
            solution
                .GetAllProjects()
                .Where(project => project.TargetFrameworkIds.Any(IsTargetFrameworkSupported));

        private static bool IsTargetFrameworkSupported(TargetFrameworkId targetFrameworkId) =>
            targetFrameworkId.UniqueString == "net5.0"
            || targetFrameworkId.UniqueString == "net6.0";
    }
}