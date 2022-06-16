using JetBrains.Lifetimes;
using JetBrains.ProjectModel.NuGet.DotNetTools;
using JetBrains.Threading;

namespace Rider.Plugins.EfCore
{
    public class EfCoreToolsTracker
    {
        private readonly NuGetDotnetToolsTracker _dotnetToolsTracker;
        private readonly JetFastSemiReenterableRWLock _lock;

        public EfCoreToolsTracker(Lifetime lifetime, NuGetDotnetToolsTracker dotnetToolsTracker)
        {
            _dotnetToolsTracker = dotnetToolsTracker;
            _lock = new JetFastSemiReenterableRWLock();
        }
    }
}