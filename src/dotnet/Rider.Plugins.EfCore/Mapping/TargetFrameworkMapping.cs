using JetBrains.Util.Dotnet.TargetFrameworkIds;
using Rider.Plugins.EfCore.Compatibility;

namespace Rider.Plugins.EfCore.Mapping
{
  public static class TargetFrameworkMapping
  {
    public static string ToTargetFrameworkString(this TargetFrameworkId targetFrameworkId) =>
      targetFrameworkId.PresentableString switch
      {
        SupportedTargetFrameworks.NetCore31 => "netcoreapp3.1",
        SupportedTargetFrameworks.NetStandard21 => "netstandard2.1",
        SupportedTargetFrameworks.NetStandard20 => "netstandard2.0",
        _ => targetFrameworkId.PresentableString
      };
  }
}
