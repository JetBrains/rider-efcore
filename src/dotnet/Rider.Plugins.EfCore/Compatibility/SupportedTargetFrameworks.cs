using System;

namespace Rider.Plugins.EfCore.Compatibility
{
  public static class SupportedTargetFrameworks
  {
    public static readonly Version OurMinimalNetCoreSupportedVersion = new(3, 1);
    public static readonly Version OurMinimalNetStandardSupportedVersion = new(2, 0);
  }
}
