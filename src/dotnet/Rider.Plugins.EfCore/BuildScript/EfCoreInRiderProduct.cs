using System;
using JetBrains.Application.BuildScript.Compile;
using JetBrains.Application.BuildScript.PackageSpecification;
using JetBrains.Application.BuildScript.Solution;
using JetBrains.Build;
using JetBrains.Rider.Backend.BuildScript;
using JetBrains.Util;

namespace Rider.Plugins.EfCore.BuildScript;

public class EfCoreInRiderProduct
{
  public static readonly SubplatformName ThisSubplatformName =
    new((RelativePath)"Plugins" / "rider-efcore" / "src" / "dotnet" / "Rider.Plugins.EfCore");

  public static readonly RelativePath DotFilesFolder = @"plugins\rider-plugins-efcore\dotnet";

  public const string ProductTechnicalName = "EfCore";

  [BuildStep]
  public static SubplatformComponentForPackagingFast[] ProductMetaDependency(AllAssembliesOnSources allassSrc)
  {
    if (!allassSrc.Has(ThisSubplatformName))
      return Array.Empty<SubplatformComponentForPackagingFast>();

    return new[]
    {
      new SubplatformComponentForPackagingFast
      (
        ThisSubplatformName,
        new JetPackageMetadata
        {
          Spec = new JetSubplatformSpec
          {
            ComplementedProductName = RiderConstants.ProductTechnicalName
          }
        }
      )
    };
  }
}
