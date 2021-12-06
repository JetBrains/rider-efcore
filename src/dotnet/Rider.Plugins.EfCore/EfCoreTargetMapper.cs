using JetBrains.Util.Dotnet.TargetFrameworkIds;

namespace Rider.Plugins.EfCore
{
    public static class EfCoreTargetMapper
    {
        public static string MapTargetFrameworkId(this TargetFrameworkId targetFrameworkId)
        {
            switch (targetFrameworkId.PresentableString)
            {
                case EfCoreSupportedTarget.NetCore31:
                    return "netcoreapp3.1";
                case EfCoreSupportedTarget.NetStandard21:
                    return "netstandard2.1";
                default:
                    return targetFrameworkId.PresentableString;
            }
        }
    }
}