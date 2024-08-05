using System.Collections.Generic;
using System.Linq;
using JetBrains.Application.Parts;
using JetBrains.ProjectModel;
using JetBrains.Util.Dotnet.TargetFrameworkIds;

namespace Rider.Plugins.EfCore.Compatibility
{
  [SolutionComponent(InstantiationEx.LegacyDefault)]
  public class SupportedMigrationsProjectsProvider
  {
    private readonly ISolution _solution;

    public SupportedMigrationsProjectsProvider(ISolution solution)
    {
      _solution = solution;
    }

    public IEnumerable<IProject> GetSupportedMigrationProjects()
    {
      var supportedMigrationProjects = _solution.GetAllProjects()
        .Where(project => project.TargetFrameworkIds.Any(IsSupportedInMigrationsProject))
        .Where(project => project.ProjectFileLocation.ExtensionNoDot == "csproj");

      return supportedMigrationProjects;
    }

    private static bool IsSupportedInMigrationsProject(TargetFrameworkId targetFrameworkId) =>
      targetFrameworkId.UniqueString.StartsWith(SupportedTargetFrameworks.Net5)
      || targetFrameworkId.UniqueString.StartsWith(SupportedTargetFrameworks.Net6)
      || targetFrameworkId.UniqueString.StartsWith(SupportedTargetFrameworks.Net7)
      || targetFrameworkId.UniqueString.StartsWith(SupportedTargetFrameworks.Net8)
      || targetFrameworkId.UniqueString.StartsWith(SupportedTargetFrameworks.NetCore31)
      || targetFrameworkId.UniqueString.StartsWith(SupportedTargetFrameworks.NetStandard20)
      || targetFrameworkId.UniqueString.StartsWith(SupportedTargetFrameworks.NetStandard21);
  }
}
