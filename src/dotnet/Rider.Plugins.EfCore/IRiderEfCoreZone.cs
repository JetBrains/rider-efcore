using JetBrains.Application.BuildScript.Application.Zones;
using JetBrains.Platform.RdFramework.Actions.Backend;
using JetBrains.ProjectModel.NuGet;
using JetBrains.ReSharper.Feature.Services.Daemon;
using JetBrains.ReSharper.Plugins.FSharp;
using JetBrains.ReSharper.Psi;
using JetBrains.ReSharper.Psi.CSharp;

namespace Rider.Plugins.EfCore
{
  [ZoneDefinition]
  // [ZoneDefinitionConfigurableFeature("Title", "Description", IsInProductSection: false)]
  public interface IRiderEfCoreZone : IPsiLanguageZone,
    IRequire<ILanguageCSharpZone>,
    IRequire<DaemonZone>,
    IRequire<INuGetZone>,
    IRequire<IRdActionsBackendZone>,
    IRequire<ISinceClr4HostZone>,
    IRequire<ILanguageFSharpZone>
  {
  }
}
