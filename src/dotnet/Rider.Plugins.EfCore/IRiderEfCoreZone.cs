using JetBrains.Application.BuildScript.Application.Zones;
using JetBrains.ReSharper.Feature.Services.Daemon;
using JetBrains.ReSharper.Psi;
using JetBrains.ReSharper.Psi.CSharp;

namespace Rider.Plugins.EfCore
{
    [ZoneDefinition]
    // [ZoneDefinitionConfigurableFeature("Title", "Description", IsInProductSection: false)]
    public interface IRiderEfCoreZone : IPsiLanguageZone,
        IRequire<ILanguageCSharpZone>,
        IRequire<DaemonZone>
    {
    }
}