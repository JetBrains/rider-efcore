using JetBrains.Util;

namespace ReSharperPlugin.RiderEfCore.Cli.Net6
{
    public static class EfCoreToolsClientFactory
    {
        public static EfCoreToolsClient CreateDefaultClient(ILogger logger) =>
            new EfCoreToolsClient(
                new MigrationsClient(logger),
                new DatabaseClient(logger),
                new DbContextClient(logger));
    }
}