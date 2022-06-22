using JetBrains.Util.Dotnet.TargetFrameworkIds;
using Rider.Plugins.EfCore.Compatibility;

namespace Rider.Plugins.EfCore
{
    public static class EfCoreTargetMapper
    {
        public static string MapTargetFrameworkId(this TargetFrameworkId targetFrameworkId) =>
            targetFrameworkId.PresentableString switch
            {
                SupportedTargetFrameworks.NetCore31 => "netcoreapp3.1",
                SupportedTargetFrameworks.NetStandard21 => "netstandard2.1",
                _ => targetFrameworkId.PresentableString
            };
    }
}