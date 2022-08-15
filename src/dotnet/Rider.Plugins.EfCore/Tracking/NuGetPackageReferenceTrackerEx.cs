using System;
using System.Linq;
using JetBrains.ProjectModel;
using JetBrains.ProjectModel.NuGet.Packaging;

namespace Rider.Plugins.EfCore.Tracking
{
  public static class NuGetPackageReferenceTrackerEx
  {
    /// <summary>
    /// TODO: This is a temporary method that is used as a replacement of <see cref="NuGetPackageReferenceTracker.HasInstalledPackage"/> that works incorrectly.
    /// </summary>
    public static bool HasPackage(this NuGetPackageReferenceTracker tracker, IProject project, string packageId) =>
      tracker.GetInstalledPackages(project)
        .Any(x => string.Equals(x.PackageIdentity.Id, packageId, StringComparison.OrdinalIgnoreCase));
  }
}
