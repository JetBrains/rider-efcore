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

    private static bool IsSupportedInMigrationsProject(TargetFrameworkId targetFrameworkId)
    {
      return targetFrameworkId.IsNetCoreApp && targetFrameworkId.Version >= SupportedTargetFrameworks.OurMinimalNetCoreSupportedVersion ||
             targetFrameworkId.IsNetStandard && targetFrameworkId.Version >= SupportedTargetFrameworks.OurMinimalNetStandardSupportedVersion;
    }
  }
}
