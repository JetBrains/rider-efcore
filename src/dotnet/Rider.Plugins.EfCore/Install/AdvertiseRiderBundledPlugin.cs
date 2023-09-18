using JetBrains.Build;
using JetBrains.Rider.Backend.Install;
using Rider.Plugins.EfCore.BuildScript;

namespace Rider.Plugins.EfCore.Install;

public static class AdvertiseRiderBundledPlugin
{
  [BuildStep]
  public static RiderBundledProductArtifact ShipEfCoreWithRider()
  {
    return new RiderBundledProductArtifact(
      EfCoreInRiderProduct.ProductTechnicalName,
      EfCoreInRiderProduct.ThisSubplatformName,
      EfCoreInRiderProduct.DotFilesFolder,
      allowCommonPluginFiles: false);
  }
}
