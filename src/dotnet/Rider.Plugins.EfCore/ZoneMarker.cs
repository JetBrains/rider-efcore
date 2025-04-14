using JetBrains.Application.BuildScript.Application.Zones;

namespace Rider.Plugins.EfCore
{
  [ZoneMarker]
  public class ZoneMarker : IRequire<IRiderEfCoreZone>;
}

