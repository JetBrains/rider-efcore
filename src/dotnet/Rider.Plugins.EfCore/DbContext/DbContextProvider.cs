using System.Collections.Generic;
using System.Linq;
using JetBrains.ProjectModel;
using JetBrains.ReSharper.Psi;
using JetBrains.ReSharper.Psi.Modules;
using JetBrains.RiderTutorials.Utils;
using Rider.Plugins.EfCore.Extensions;
using Rider.Plugins.EfCore.Migrations;
using Rider.Plugins.EfCore.Rd;

namespace Rider.Plugins.EfCore.DbContext
{
  [SolutionComponent]
  public class DbContextProvider
  {
    public IEnumerable<DbContextInfo> GetDbContexts(IProject project)
    {
      using (CompilationContextCookie.GetExplicitUniversalContextIfNotSet())
      {
        var foundDbContexts = project
          .GetPsiModules()
          .SelectMany(module => module.FindInheritorsOf(EfCoreKnownTypeNames.DbContextBaseClass))
          // To get around of multiple modules (multiple target frameworks)
          .Distinct(dbContextClass => dbContextClass.GetFullClrName())
          .TrySelect<IClass, DbContextInfo>(TryGetDbContextInfo)
          .ToList();

        return foundDbContexts;
      }
    }

    private static bool TryGetDbContextInfo(IClass @class, out DbContextInfo dbContextInfo)
    {
      dbContextInfo = null;

      if (@class.IsAbstract)
        return false;

      dbContextInfo = new DbContextInfo(@class.ShortName, @class.GetFullClrName());

      return true;
    }
  }
}
