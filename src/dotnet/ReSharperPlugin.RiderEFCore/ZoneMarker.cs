using JetBrains.Application.BuildScript.Application.Zones;
using JetBrains.Platform.RdFramework;
using JetBrains.ProjectModel;

namespace ReSharperPlugin.RiderEfCore
{
    [ZoneMarker]
    public class ZoneMarker : IRequire<IRiderEfCoreZone>, IRequire<IProjectModelZone>, IRequire<IRdFrameworkZone>
    {
    }
}