using System;
using JetBrains.Application.Parts;
using JetBrains.Lifetimes;
using JetBrains.ProjectModel;
using JetBrains.ProjectModel.NuGet.DotNetTools;
using JetBrains.ProjectModel.Tasks;
using JetBrains.Threading;
using JetBrains.Util;
using Rider.Plugins.EfCore.Logging;

namespace Rider.Plugins.EfCore.Tracking
{
  [SolutionComponent(InstantiationEx.LegacyDefault)]
  public class SolutionTracker
  {
    private readonly Lifetime _lifetime;
    private readonly NugetDependenciesListener _nugetListener;
    private readonly SolutionStructureChangedListener _solutionListener;
    private readonly ISolutionLoadTasksScheduler _solutionLoadScheduler;
    private readonly SolutionDotnetToolsTracker _dotnetToolsTracker;
    private readonly ILogger _logger;

    private readonly JetFastSemiReenterableRWLock _lock = new JetFastSemiReenterableRWLock();

    public Action OnAfterSolutionLoad { get; set; }
    public Action OnAfterSolutionUpdate { get; set; }
    public Action OnAfterNuGetUpdate { get; set; }
    public Action<DotNetToolCache> OnAfterToolsCacheUpdate { get; set; }

    public SolutionTracker(
      Lifetime lifetime,
      NugetDependenciesListener nugetListener,
      SolutionStructureChangedListener solutionListener,
      ISolutionLoadTasksScheduler solutionLoadScheduler,
      SolutionDotnetToolsTracker dotnetToolsTracker,
      ILogger logger)
    {
      _lifetime = lifetime;
      _nugetListener = nugetListener;
      _solutionListener = solutionListener;
      _solutionLoadScheduler = solutionLoadScheduler;
      _dotnetToolsTracker = dotnetToolsTracker;
      _logger = logger;
    }

    public void Setup()
    {
      _dotnetToolsTracker.DotNetToolCache.Change.Advise(_lifetime, args =>
      {
        if (!args.HasNew || args.New is null) return;
        using var _ = _lock.UsingWriteLock();

        var cache = args.New;

        _logger.LogFlow($"{nameof(SolutionDotnetToolsTracker)}.DotNetToolCache.Change",
          $"Calling {nameof(OnAfterToolsCacheUpdate)}");

        OnAfterToolsCacheUpdate?.Invoke(cache);
      });

      _nugetListener.ProjectsUpdated += () =>
      {
        _logger.LogFlow($"{nameof(NugetDependenciesListener)}.ProjectsUpdated",
          $"Calling {nameof(OnAfterNuGetUpdate)}");

        OnAfterNuGetUpdate?.Invoke();
      };

      _solutionLoadScheduler.EnqueueTask(
        new SolutionLoadTask(
          GetType(),
          SolutionLoadTaskKinds.AfterDone,
          () =>
          {
            _logger.LogFlow($"{nameof(ISolutionLoadTasksScheduler)}.AfterDone",
              $"Calling {nameof(OnAfterSolutionLoad)}");

            OnAfterSolutionLoad?.Invoke();

            _solutionListener.SolutionChanged += () =>
            {
              _logger.LogFlow($"{nameof(SolutionStructureChangedListener)}.SolutionChanged",
                $"Calling {nameof(OnAfterSolutionUpdate)}");

              OnAfterSolutionUpdate?.Invoke();
            };
          }
        )
      );
    }

    public void RefreshDotNetToolsCache() => _dotnetToolsTracker.QueueManifestUpdate();
  }
}
