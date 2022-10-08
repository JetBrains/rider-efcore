using System.Collections.Generic;
using System.Linq;
using FSharp.Compiler.Symbols;
using JetBrains.Metadata.Reader.API;
using JetBrains.ProjectModel;
using JetBrains.ReSharper.Plugins.FSharp.Psi;
using JetBrains.ReSharper.Plugins.FSharp.Psi.Impl;
using JetBrains.ReSharper.Plugins.FSharp.Psi.Util;
using JetBrains.ReSharper.Psi;
using JetBrains.ReSharper.Psi.CSharp;
using JetBrains.ReSharper.Psi.Modules;
using JetBrains.RiderTutorials.Utils;
using Rider.Plugins.EfCore.Extensions;
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
          .SelectMany(module => module.FindInheritorsOf(project, EfCoreKnownTypeNames.MigrationBaseClass))
          .Distinct(migrationClass =>
            migrationClass.GetFullClrName()) // To get around of multiple modules (multiple target frameworks)
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
      var migrationAttribute = @class.GetAttributeInstance(KnownAttributes.MigrationAttribute);
      var dbContextAttribute = @class.GetAttributeInstance(KnownAttributes.DbContextAttribute);

      if (dbContextAttribute is null || migrationAttribute is null)
      {
        return false;
      }

      var migrationLongName = migrationAttribute
        .PositionParameter(0)
        .ConstantValue
        .StringValue;

      var dbContextClass = ExtractDbContextClrName(dbContextAttribute);

      if (migrationLongName is null || dbContextClass is null)
      {
        return false;
      }

      var migrationFolderAbsolutePath = @class.GetSourceFiles()
        .FirstOrDefault()
        .GetLocation().Directory.FileAccessPath;

      var language = @class.PresentationLanguage switch
      {
        CSharpLanguage _ => Language.CSharp,
        FSharpLanguage _ => Language.FSharp,
        _ => Language.Unknown
      };

      migrationInfo = new MigrationInfo(
        dbContextClass.FullName,
        migrationShortName,
        migrationLongName,
        migrationFolderAbsolutePath,
        language);

      return true;
    }

    private static IClrTypeName ExtractDbContextClrName(IAttributeInstance attributeInstance) =>
      attributeInstance switch
      {
        FSharpAttributeInstance fsharpAttribute =>
          (fsharpAttribute
            .AttrConstructorArgs
            .First()
            .Item2 as FSharpType)?
          .TypeDefinition
          .GetClrName(),

        _ => attributeInstance
          .PositionParameter(0)
          .TypeValue
          ?.GetScalarType()
          ?.GetClrName()
      };
  }
}
