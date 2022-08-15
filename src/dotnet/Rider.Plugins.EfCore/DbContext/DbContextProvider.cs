using System.Collections.Generic;
using System.Linq;
using JetBrains.ProjectModel;
using JetBrains.ReSharper.Psi;
using JetBrains.ReSharper.Psi.Modules;
using JetBrains.ReSharper.Resources.Shell;
using JetBrains.RiderTutorials.Utils;
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
          .SelectMany(module => module.FindInheritorsOf(project, EfCoreKnownTypeNames.DbContextBaseClass))
          .Distinct(dbContextClass =>
            dbContextClass.GetFullClrName()) // To get around of multiple modules (multiple target frameworks)
          .Select(dbContextClass =>
            new DbContextInfo(dbContextClass.ShortName, dbContextClass.GetFullClrName()))
          .ToList();

        return foundDbContexts;
      }
    }
  }
}
