using System.Collections.Generic;
using System.Linq;
using JetBrains.Application.BuildScript.PreCompile.Autofix;
using JetBrains.Application.BuildScript.Solution;
using JetBrains.Build;

namespace Rider.Plugins.EfCore.BuildScript
{
  public static class DefineRiderEfCoreConstants
  {
    [BuildStep]
    public static IEnumerable<AutofixAllowedDefineConstant> YieldAllowedDefineConstantsForEfCorePlugin()
    {
      var constants = new List<string>();

      constants.AddRange(new[] {"$(DefineConstants)", "RIDER"});

      return constants.SelectMany(s => new []
      {
        new AutofixAllowedDefineConstant(new SubplatformName("Plugins\\rider-efcore\\src\\dotnet\\Rider.Plugins.EfCore"), s)
      });
    }
  }
}
