using JetBrains.Util;
using ReSharperPlugin.RiderEfCore.Cli.Net6.Abstractions;
using ReSharperPlugin.RiderEfCore.Cli.Net6.Models;

namespace ReSharperPlugin.RiderEfCore.Cli.Net6
{
    public class DatabaseClient : EfCoreToolsSectionBase, IDatabaseClient
    {
        public DatabaseClient(ILogger logger) : base(logger)
        {
        }

        public EfCoreCommandResult Drop(EfCoreCommonOptions options, bool? force = default, bool? dryRun = default)
        {
            throw new System.NotImplementedException();
        }

        public EfCoreCommandResult Update(EfCoreCommonOptions options, string migration, string connectionString = default)
        {
            // TODO: Support connection strings
            var command = CreateEfCoreCommand(EfCoreCommandNames.Database.Update, options, args => args
                .Add(migration));

            return ExecuteLogged(command);
        }
    }
}