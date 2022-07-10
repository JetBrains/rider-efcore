using System;
using JetBrains.Diagnostics;
using JetBrains.Lifetimes;
using JetBrains.ProjectModel;
using JetBrains.ProjectModel.NuGet.Packaging;
using JetBrains.Util;

namespace Rider.Plugins.EfCore.Compatibility
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
          _logger.Log(LoggingLevel.WARN, "[EF Core]: NugetDependenciesListener.InitialProcessingIsCompleted");
          ProjectsUpdated?.Invoke();
          SetupUpdateListeners();
        }
      });
    }

    private void SetupUpdateListeners()
    {
      _nuGetPackageReferenceTracker.ProjectsUpdated.Advise(_lifetime, array =>
      {
        _logger.Log(LoggingLevel.WARN, "[EF Core]: NugetDependenciesListener.ProjectsUpdated");
        ProjectsUpdated?.Invoke();
      });

      _nuGetPackageReferenceTracker.ProjectsUpdatedWithChanges.Advise(_lifetime, array =>
      {
        _logger.Log(LoggingLevel.WARN, "[EF Core]: NugetDependenciesListener.ProjectsUpdatedWithChanges");
        ProjectsUpdated?.Invoke();
      });

      _nuGetPackageReferenceTracker.ProjectsUpdatedInitial.Advise(_lifetime, array =>
      {
        _logger.Log(LoggingLevel.WARN, "[EF Core]: NugetDependenciesListener.ProjectsUpdatedInitial");
        ProjectsUpdated?.Invoke();
      });
    }
  }
}