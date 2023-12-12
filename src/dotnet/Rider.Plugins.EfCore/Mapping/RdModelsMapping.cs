﻿using System.Linq;
using JetBrains.ProjectModel;
using JetBrains.ReSharper.Psi.Util;
using Rider.Plugins.EfCore.Rd;

namespace Rider.Plugins.EfCore.Mapping
{
  public static class RdModelsMapping
  {
    public static StartupProjectInfo ToStartupProjectInfo(this IProject project) =>
      new StartupProjectInfo(
        project.TargetFrameworkIds
          .Where(frameworkId => !frameworkId.IsNetStandard)
          .Select(frameworkId => frameworkId.ToTargetFrameworkString())
          .ToList(),
        project.Guid,
        project.Name,
        project.ProjectFileLocation.FullPath,
        project.GetDefaultNamespace() ?? string.Empty);

    public static MigrationsProjectInfo ToMigrationsProjectInfo(this IProject project) =>
      new MigrationsProjectInfo(
        project.Guid,
        project.Name,
        project.ProjectFileLocation.FullPath,
        project.GetDefaultNamespace() ?? string.Empty);
  }
}
