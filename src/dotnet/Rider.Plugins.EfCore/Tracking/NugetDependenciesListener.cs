using System;
using JetBrains.Lifetimes;
using JetBrains.ProjectModel;
using JetBrains.ProjectModel.NuGet.Packaging;
using JetBrains.Util;
using Rider.Plugins.EfCore.Logging;

namespace Rider.Plugins.EfCore.Tracking
{
  [SolutionComponent]
  public class NugetDependenciesListener
  {
    private readonly NuGetPackageReferenceTracker _nuGetPackageReferenceTracker;
    private readonly Lifetime _lifetime;
    private readonly ILogger _logger;

    public Action ProjectsUpdated { get; set; }

    public NugetDependenciesListener(
      NuGetPackageReferenceTracker nuGetPackageReferenceTracker,
      Lifetime lifetime,
      ILogger logger)
    {
      _nuGetPackageReferenceTracker = nuGetPackageReferenceTracker;
      _lifetime = lifetime;
      _logger = logger;

      SetupInitialProcessingListener();
    }

    private void SetupInitialProcessingListener()
    {
      _nuGetPackageReferenceTracker.InitialProcessingIsCompleted.Change.Advise(_lifetime, args =>
      {
        if (args.New && !args.Old)
        {
          _logger.LogFlow($"{nameof(NugetDependenciesListener)}.InitialProcessingIsCompleted.Change",
            $"Calling {nameof(ProjectsUpdated)}");
          ProjectsUpdated?.Invoke();
          SetupUpdateListeners();
        }
      });
    }

    private void SetupUpdateListeners()
    {
      _nuGetPackageReferenceTracker.ProjectsUpdated.Advise(_lifetime, array =>
      {
        _logger.LogFlow($"{nameof(NuGetPackageReferenceTracker)}.ProjectsUpdated",
          $"Calling {nameof(ProjectsUpdated)}");
        ProjectsUpdated?.Invoke();
      });

      _nuGetPackageReferenceTracker.ProjectsUpdatedWithChanges.Advise(_lifetime, array =>
      {
        _logger.LogFlow($"{nameof(NuGetPackageReferenceTracker)}.ProjectsUpdatedWithChanges",
          $"Calling {nameof(ProjectsUpdated)}");
        ProjectsUpdated?.Invoke();
      });

      _nuGetPackageReferenceTracker.ProjectsUpdatedInitial.Advise(_lifetime, array =>
      {
        _logger.LogFlow($"{nameof(NuGetPackageReferenceTracker)}.ProjectsUpdatedInitial",
          $"Calling {nameof(ProjectsUpdated)}");
        ProjectsUpdated?.Invoke();
      });
    }
  }
}
