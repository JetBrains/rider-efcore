using System.Linq;
using JetBrains.ProjectModel;
using JetBrains.ReSharper.Psi.Util;
using Rider.Plugins.EfCore.Rd;

namespace Rider.Plugins.EfCore.Mapping
{
  public static class RdModelsMapping
  {
    public static StartupProjectInfo ToStartupProjectInfo(this IProject project) =>
      new StartupProjectInfo(
        project.Guid,
        project.Name,
        project.ProjectFileLocation.FullPath,
        project.TargetFrameworkIds
          .Where(frameworkId => !frameworkId.IsNetStandard)
          .Select(frameworkId => frameworkId.ToTargetFrameworkString())
          .ToList(),
        project.GetDefaultNamespace() ?? string.Empty);

    public static MigrationsProjectInfo ToMigrationsProjectInfo(this IProject project) =>
      new MigrationsProjectInfo(
        project.Guid,
        project.Name,
        project.ProjectFileLocation.FullPath,
        project.GetDefaultNamespace() ?? string.Empty);
  }
}
