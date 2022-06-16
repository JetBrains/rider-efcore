using JetBrains.Application.BuildScript.Application.Zones;
using JetBrains.Platform.RdFramework;
using JetBrains.ProjectModel;
using JetBrains.ProjectModel.NuGet;

namespace Rider.Plugins.EfCore
{
    [ZoneMarker]
    public class ZoneMarker
        : IRequire<IRiderEfCoreZone>, IRequire<IProjectModelZone>, IRequire<IRdFrameworkZone>
    {
    }
}