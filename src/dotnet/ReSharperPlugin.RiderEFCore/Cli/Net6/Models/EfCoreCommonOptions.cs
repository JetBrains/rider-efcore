using JetBrains.Annotations;

namespace ReSharperPlugin.RiderEfCore.Cli.Net6.Models
{
    public class EfCoreCommonOptions
    {
        [NotNull]
        public string MigrationsProject { get; }

        [NotNull]
        public string StartupProject { get; }

        public EfCoreCommonOptions([NotNull] string migrationsProject, [NotNull] string startupProject)
        {
            MigrationsProject = migrationsProject;
            StartupProject = startupProject;
        }
    }
}