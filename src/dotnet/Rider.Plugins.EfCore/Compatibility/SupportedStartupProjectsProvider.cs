using System.Collections.Generic;
using System.Linq;
using JetBrains.ProjectModel;
using JetBrains.ProjectModel.NuGet.Packaging;
using JetBrains.Util;
using JetBrains.Util.Dotnet.TargetFrameworkIds;
using Rider.Plugins.EfCore.Logging;
using Rider.Plugins.EfCore.Tracking;

namespace Rider.Plugins.EfCore.Compatibility
{
  [SolutionComponent]
  public class SupportedStartupProjectsProvider
  {
    private readonly ISolution _solution;
    private readonly NuGetPackageReferenceTracker _nugetTracker;
    private readonly ILogger _logger;

    public SupportedStartupProjectsProvider(ISolution solution, NuGetPackageReferenceTracker nugetTracker,
      ILogger logger)
    {
      _solution = solution;
      _nugetTracker = nugetTracker;
      _logger = logger;
    }

    public IEnumerable<IProject> GetSupportedStartupProjects()
    {
      var projectsWithNugetPacks = _solution.GetAllProjects()
        .Where(StartupProjectPackagesInstalled)
        .ToList();

      var referencingProjects = projectsWithNugetPacks.SelectMany(GetReferencingProjects).ToList();

      _logger.LogFlow($"{nameof(SupportedStartupProjectsProvider)}.{nameof(GetSupportedStartupProjects)}",
        $"Projects with nuget packs: {projectsWithNugetPacks.Count}, referencing projects: {referencingProjects.Count}");

      var result = projectsWithNugetPacks
        .Concat(referencingProjects)
        .Where(project => project.TargetFrameworkIds.Any(IsSupportedTargetFramework))
        .Distinct();

      return result;
    }

    private bool StartupProjectPackagesInstalled(IProject project) =>
      _nugetTracker.HasPackage(project, KnownNuGetPackages.EfCoreToolsNugetId)
      || _nugetTracker.HasPackage(project, KnownNuGetPackages.EfCoreDesignNugetId);

    private IEnumerable<IProject> GetReferencingProjects(IProject project) =>
      project.TargetFrameworkIds
        .SelectMany(x => project.GetReferencingProjectsEx(x))
        .Select(x => x.Value)
        .ToList();

    private static bool IsSupportedTargetFramework(TargetFrameworkId targetFrameworkId) =>
      targetFrameworkId.UniqueString.StartsWith(SupportedTargetFrameworks.Net5)
      || targetFrameworkId.UniqueString.StartsWith(SupportedTargetFrameworks.Net6)
      || targetFrameworkId.UniqueString.StartsWith(SupportedTargetFrameworks.Net7)
      || targetFrameworkId.UniqueString.StartsWith(SupportedTargetFrameworks.NetCore31);
  }
}
