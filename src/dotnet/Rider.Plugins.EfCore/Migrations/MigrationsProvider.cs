using System.Collections.Generic;
using System.Linq;
using JetBrains.Application.Parts;
using JetBrains.ProjectModel;
using JetBrains.ReSharper.Psi;
using JetBrains.ReSharper.Psi.Modules;
using Rider.Plugins.EfCore.Extensions;
using Rider.Plugins.EfCore.Rd;

namespace Rider.Plugins.EfCore.Migrations
{
  [SolutionComponent(InstantiationEx.LegacyDefault)]
  public class MigrationsProvider
  {
    public bool HasMigrations(IProject project, string dbContextFullName)
    {
      using (CompilationContextCookie.GetExplicitUniversalContextIfNotSet())
      {
        var projectHasMigrations = project.GetPsiModules()
          ?.SelectMany(module => module.FindInheritorsOf(EfCoreKnownTypeNames.MigrationBaseClass))
          .TrySelect<IClass, MigrationInfo>(TryGetMigrationInfo)
          .Any(migrationInfo =>
            migrationInfo != null && migrationInfo.DbContextClassFullName == dbContextFullName);

        return projectHasMigrations ?? false;
      }
    }

    public IEnumerable<MigrationInfo> GetMigrations(IProject project, string dbContextFullName)
    {
      using (CompilationContextCookie.GetExplicitUniversalContextIfNotSet())
      {
        var foundMigrations = project
          .GetPsiModules()
          .SelectMany(module => module.FindInheritorsOf(EfCoreKnownTypeNames.MigrationBaseClass))
          // To get around of multiple modules (multiple target frameworks)
          .Distinct(migrationClass => migrationClass.GetClrName().FullName)
          .TrySelect<IClass, MigrationInfo>(TryGetMigrationInfo)
          .Where(m => m.DbContextClassFullName == dbContextFullName)
          .ToList();

        return foundMigrations;
      }
    }

    private static bool TryGetMigrationInfo(IClass @class, out MigrationInfo migrationInfo)
    {
      migrationInfo = null;

      var migrationShortName = @class.ShortName;
      var migrationAttribute = @class.GetAttributeInstance("MigrationAttribute");
      var dbContextAttribute = @class.GetAttributeInstance("DbContextAttribute");

      if (dbContextAttribute is null || migrationAttribute is null)
        return false;

      var migrationLongName = migrationAttribute
        .PositionParameter(0)
        .ConstantValue
        .StringValue;

      var dbContextClass = dbContextAttribute
        .PositionParameter(0)
        .TypeValue
        ?.GetScalarType()
        ?.GetClrName();

      if (migrationLongName is null || dbContextClass is null)
        return false;

      var migrationFolderAbsolutePath = @class.GetSourceFiles()
        .FirstOrDefault()
        .GetLocation().Directory.FileAccessPath;

      migrationInfo = new MigrationInfo(
        dbContextClass.FullName,
        migrationShortName,
        migrationLongName,
        migrationFolderAbsolutePath);

      return true;
    }
  }
}
