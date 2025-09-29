using System;
using Rider.Plugins.EfCore.Compatibility;
using Rider.Plugins.EfCore.Rd;

namespace Rider.Plugins.EfCore.Mapping
{
  public static class TargetFrameworkMapping
  {
    public static TargetFrameworkId ToRdTargetFramework(
      this JetBrains.Util.Dotnet.TargetFrameworkIds.TargetFrameworkId targetFrameworkId) =>
      new(
        targetFrameworkId.Version.ToRdTargetFrameworkVersion(),
        targetFrameworkId.TryGetShortIdentifier() ?? targetFrameworkId.PresentableString);

    public static TargetFrameworkVersion ToRdTargetFrameworkVersion(this Version version) =>
      new(version.Major, version.Minor, version.Build);
  }
}
