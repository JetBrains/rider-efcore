using ReSharperPlugin.RiderEfCore.Cli.Net6.Abstractions;

namespace ReSharperPlugin.RiderEfCore.Cli.Net6
{
    public class EfCoreToolsClient : IEfCoreToolsClient
    {
        public IMigrationsClient Migrations { get; }

        public IDatabaseClient Database { get; }

        public IDbContextClient DbContext { get; }

        public EfCoreToolsClient(IMigrationsClient migrationsClient, IDatabaseClient databaseClient, IDbContextClient dbContextClient)
        {
            Migrations = migrationsClient;
            Database = databaseClient;
            DbContext = dbContextClient;
        }
    }
}