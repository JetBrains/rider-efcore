using CliWrap.Builders;

namespace ReSharperPlugin.RiderEfCore.Cli
{
    public static class CliWrapExtensions
    {
        public static ArgumentsBuilder AddEfCoreMigrationsProject(this ArgumentsBuilder builder, string projectFolder) =>
            builder.Add("--project").Add(projectFolder);
    }
}