using System.Collections.Generic;
using System.Linq;
using JetBrains.ProjectModel;
using JetBrains.ReSharper.Psi;
using JetBrains.ReSharper.Psi.Modules;
using JetBrains.ReSharper.Resources.Shell;
using JetBrains.RiderTutorials.Utils;
using Rider.Plugins.EfCore.Mapping;
using Rider.Plugins.EfCore.Rd;

namespace Rider.Plugins.EfCore.Migrations
{
  [SolutionComponent]
  public class MigrationsProvider
  {
    public bool HasMigrations(IProject project, string dbContextFullName)
    {
      using (CompilationContextCookie.GetExplicitUniversalContextIfNotSet())
      {
        var projectHasMigrations = project.GetPsiModules()
          ?.SelectMany(module => module.FindInheritorsOf(project, EfCoreKnownTypeNames.MigrationBaseClass))
          .Select(cl => cl.ToMigrationInfo())
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
          .SelectMany(module => module.FindInheritorsOf(project, EfCoreKnownTypeNames.MigrationBaseClass))
          .Distinct(migrationClass =>
            migrationClass.GetFullClrName()) // To get around of multiple modules (multiple target frameworks)
          .Select(migrationClass => migrationClass.ToMigrationInfo())
          .Where(m => m.DbContextClassFullName == dbContextFullName)
          .ToList();

        return foundMigrations;
      }
    }
  }
}
