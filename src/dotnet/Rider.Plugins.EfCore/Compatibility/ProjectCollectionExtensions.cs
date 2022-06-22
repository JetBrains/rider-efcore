using System;
using System.Collections.Generic;
using System.Linq;
using JetBrains.ProjectModel;
using JetBrains.Util.Dotnet.TargetFrameworkIds;

namespace Rider.Plugins.EfCore.Compatibility
{
    public static class CompatibilityExtensions
    {
        public static IEnumerable<IProject> GetSupportedDotnetProjects(this IProjectCollection solution,
            Func<TargetFrameworkId, bool> condition) =>
            solution.GetAllProjects()
                .Where(project => project.TargetFrameworkIds.Any(condition));
    }
}