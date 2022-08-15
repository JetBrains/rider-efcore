using System;
using JetBrains.Diagnostics;
using JetBrains.ProjectModel;
using JetBrains.ProjectModel.ProjectsHost.Impl;
using JetBrains.ProjectModel.ProjectsHost.SolutionHost;
using JetBrains.Util;
using Rider.Plugins.EfCore.Logging;

namespace Rider.Plugins.EfCore.Tracking
{
  [SolutionComponent]
  public class SolutionStructureChangedListener : SolutionHostSyncListener
  {
    private readonly ILogger _logger;
    public Action SolutionChanged { get; set; }

    public SolutionStructureChangedListener(ILogger logger)
    {
      _logger = logger;
    }

    public override void AfterUpdateProject(ProjectHostChange change)
    {
      _logger.LogFlow($"{nameof(SolutionStructureChangedListener)}.{nameof(AfterUpdateProject)}",
        $"Calling {nameof(SolutionChanged)}");

      SolutionChanged?.Invoke();
    }

    public override void AfterUpdateProjects(ProjectStructureChange change)
    {
      _logger.LogFlow($"{nameof(SolutionStructureChangedListener)}.{nameof(AfterUpdateProjects)}",
        $"Calling {nameof(SolutionChanged)}");

      SolutionChanged?.Invoke();
    }

    public override void AfterUpdateSolution(SolutionStructureChange change)
    {
      _logger.LogFlow($"{nameof(SolutionStructureChangedListener)}.{nameof(AfterUpdateSolution)}",
        $"Calling {nameof(SolutionChanged)}");

      SolutionChanged?.Invoke();
    }

    public override void AfterRemoveProject(ProjectHostChange change)
    {
      _logger.LogFlow($"{nameof(SolutionStructureChangedListener)}.{nameof(AfterRemoveProject)}",
        $"Calling {nameof(SolutionChanged)}");

      SolutionChanged?.Invoke();
    }
  }
}
