using System;
using JetBrains.Diagnostics;
using JetBrains.ProjectModel;
using JetBrains.ProjectModel.ProjectsHost.Impl;
using JetBrains.ProjectModel.ProjectsHost.SolutionHost;
using JetBrains.Util;

namespace Rider.Plugins.EfCore.Compatibility
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
            _logger.Log(LoggingLevel.WARN, "SolutionStructureChangedListener.AfterUpdateProject");

            SolutionChanged?.Invoke();
        }

        public override void AfterUpdateProjects(ProjectStructureChange change)
        {
            _logger.Log(LoggingLevel.WARN, "SolutionStructureChangedListener.AfterUpdateProjects");

            SolutionChanged?.Invoke();
        }

        public override void AfterUpdateSolution(SolutionStructureChange change)
        {
            _logger.Log(LoggingLevel.WARN, "SolutionStructureChangedListener.AfterUpdateSolution");

            SolutionChanged?.Invoke();
        }

        public override void AfterRemoveProject(ProjectHostChange change)
        {
            _logger.Log(LoggingLevel.WARN, "SolutionStructureChangedListener.AfterRemoveProject");

            SolutionChanged?.Invoke();
        }
    }
}