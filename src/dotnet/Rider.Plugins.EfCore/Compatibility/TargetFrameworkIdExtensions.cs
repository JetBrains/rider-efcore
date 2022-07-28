using JetBrains.Util.Dotnet.TargetFrameworkIds;

namespace Rider.Plugins.EfCore.Compatibility
{
    public static class TargetFrameworkIdExtensions
    {
        public static bool IsSupportedInMigrationsProject(this TargetFrameworkId targetFrameworkId) =>
            targetFrameworkId.UniqueString.StartsWith(SupportedTargetFrameworks.Net5)
            || targetFrameworkId.UniqueString.StartsWith(SupportedTargetFrameworks.Net6)
            || targetFrameworkId.UniqueString.StartsWith(SupportedTargetFrameworks.Net7)
            || targetFrameworkId.UniqueString.StartsWith(SupportedTargetFrameworks.NetCore31)
            || targetFrameworkId.UniqueString.StartsWith(SupportedTargetFrameworks.NetStandard20)
            || targetFrameworkId.UniqueString.StartsWith(SupportedTargetFrameworks.NetStandard21);

        public static bool IsSupportedInStartupProject(this TargetFrameworkId targetFrameworkId) =>
            targetFrameworkId.UniqueString.StartsWith(SupportedTargetFrameworks.Net5)
            || targetFrameworkId.UniqueString.StartsWith(SupportedTargetFrameworks.Net6)
            || targetFrameworkId.UniqueString.StartsWith(SupportedTargetFrameworks.Net7)
            || targetFrameworkId.UniqueString.StartsWith(SupportedTargetFrameworks.NetCore31);
    }
}
