using JetBrains.Application.BuildScript.Application.Zones;
using JetBrains.Application.Environment;
using JetBrains.Platform.RdFramework;
using JetBrains.Platform.RdFramework.Actions.Backend;
using JetBrains.ProjectModel;
using JetBrains.ProjectModel.NuGet;
using JetBrains.ProjectModel.ProjectsHost.SolutionHost;
using JetBrains.ReSharper.Feature.Services.Daemon;
using JetBrains.ReSharper.Psi;
using JetBrains.ReSharper.Psi.CSharp;
using JetBrains.Rider.Backend.Env;
using Rider.Plugins.EfCore;

namespace Rider.Plugins.EfCore
{
  [ZoneDefinition]
  // [ZoneDefinitionConfigurableFeature("Title", "Description", IsInProductSection: false)]
  public interface IRiderEfCoreZone : IZone,
    IRequire<IPsiLanguageZone>,
    IRequire<ILanguageCSharpZone>,
    IRequire<DaemonZone>,
    IRequire<INuGetZone>,
    IRequire<IRdActionsBackendZone>,
    IRequire<IHostSolutionZone>,
    IRequire<IProjectModelZone>,
    IRequire<IRdFrameworkZone>
  {
  }
}

namespace Rider.Plugins.EfCoreActivator
{
  [ZoneActivator]
  [ZoneMarker(typeof(IRiderBackendFullFeatureEnvironmentZone))]
  public class EfPluginActivator : IActivate<IRiderEfCoreZone>;
}
