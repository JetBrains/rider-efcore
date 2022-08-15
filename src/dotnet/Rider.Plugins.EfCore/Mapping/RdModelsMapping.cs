using System.Linq;
using JetBrains.ProjectModel;
using JetBrains.ReSharper.Psi;
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

    public static MigrationInfo ToMigrationInfo(this IClass @class)
    {
      var migrationShortName = @class.ShortName;
      var migrationAttribute = @class.GetAttributeInstance("MigrationAttribute");
      var dbContextAttribute = @class.GetAttributeInstance("DbContextAttribute");

      var migrationLongName = migrationAttribute.PositionParameter(0).ConstantValue.Value as string;

      var dbContextClass = dbContextAttribute.PositionParameter(0).TypeValue?.GetScalarType()?
        .GetClrName();

      if (migrationLongName is null || dbContextClass is null)
      {
        return null;
      }

      var migrationFolderAbsolutePath = @class.GetSourceFiles()
        .FirstOrDefault()
        .GetLocation().Directory.FileAccessPath;

      return new MigrationInfo(
        dbContextClass.FullName,
        migrationShortName,
        migrationLongName,
        migrationFolderAbsolutePath);
    }

    private static IAttributeInstance GetAttributeInstance(this IAttributesSet @class, string attributeShortName) =>
      @class
        .GetAttributeInstances(AttributesSource.All)
        .SingleOrDefault(attribute => attribute.GetAttributeShortName() == attributeShortName);
  }
}
