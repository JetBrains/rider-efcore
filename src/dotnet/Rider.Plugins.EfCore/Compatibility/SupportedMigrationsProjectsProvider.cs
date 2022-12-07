using System.Collections.Generic;
using System.Linq;
using JetBrains.ProjectModel;
using JetBrains.Util.Dotnet.TargetFrameworkIds;

namespace Rider.Plugins.EfCore.Compatibility
{
  [SolutionComponent]
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
        .Where(project => project.TargetFrameworkIds.Any(IsSupportedTargetFramework))
        .Where(project => IsSupportedProjectExtension(project.ProjectFileLocation.ExtensionNoDot));

      return supportedMigrationProjects;
    }

    private static bool IsSupportedProjectExtension(string extensionNoDot) =>
      extensionNoDot == "csproj"
      || extensionNoDot == "fsproj";

    private static bool IsSupportedTargetFramework(TargetFrameworkId targetFrameworkId) =>
      targetFrameworkId.UniqueString.StartsWith(SupportedTargetFrameworks.Net5)
      || targetFrameworkId.UniqueString.StartsWith(SupportedTargetFrameworks.Net6)
      || targetFrameworkId.UniqueString.StartsWith(SupportedTargetFrameworks.Net7)
      || targetFrameworkId.UniqueString.StartsWith(SupportedTargetFrameworks.NetCore31)
      || targetFrameworkId.UniqueString.StartsWith(SupportedTargetFrameworks.NetStandard20)
      || targetFrameworkId.UniqueString.StartsWith(SupportedTargetFrameworks.NetStandard21);
  }
}
