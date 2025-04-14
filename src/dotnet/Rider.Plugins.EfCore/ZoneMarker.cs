using JetBrains.Application.BuildScript.Application.Zones;
using JetBrains.Application.Environment;
using JetBrains.Platform.RdFramework;
using JetBrains.ProjectModel;
using JetBrains.Rider.Backend.Env;

namespace Rider.Plugins.EfCore
{
  [ZoneMarker]
  public class ZoneMarker : IRequire<IRiderBackendFeatureEnvironmentZone>, IRequire<IRiderEfCoreZone>, IRequire<IProjectModelZone>, IRequire<IRdFrameworkZone>;

  [ZoneActivator]
  public class EfPluginActivator : IActivate<IRiderEfCoreZone>;
}
