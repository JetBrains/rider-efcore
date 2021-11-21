using JetBrains.Util;
using ReSharperPlugin.RiderEfCore.Cli.Extensions;
using ReSharperPlugin.RiderEfCore.Cli.Net6.Abstractions;
using ReSharperPlugin.RiderEfCore.Cli.Net6.Models;

namespace ReSharperPlugin.RiderEfCore.Cli.Net6
{
    public class MigrationsClient : EfCoreToolsSectionBase, IMigrationsClient
    {
        public MigrationsClient(ILogger logger) : base(logger)
        {
        }

        public EfCoreCommandResult Add(EfCoreCommonOptions options, string migrationName, string outputDirectory = default,
            string @namespace = default)
        {
            var command = CreateEfCoreCommand(EfCoreCommandNames.Migrations.Add, options, args => args
                .Add(migrationName)
                .AddOptional("--output-dir", outputDirectory)
                .AddOptional("--namespace", @namespace));

            return ExecuteLogged(command);
        }

        public EfCoreCommandResult Bundle(EfCoreCommonOptions options, string output = default, bool? force = default,
            bool? selfContained = default, string targetRuntime = default)
        {
            throw new System.NotImplementedException();
        }

        public EfCoreCommandResult List(EfCoreCommonOptions options, string connectionString = default, bool? noConnect = default)
        {
            throw new System.NotImplementedException();
        }

        public EfCoreCommandResult Remove(EfCoreCommonOptions options, bool? force = default)
        {
            var command = CreateEfCoreCommand(EfCoreCommandNames.Migrations.Remove, options, args => args
                .AddOptionalKey("--force", force));

            return ExecuteLogged(command);
        }

        public EfCoreCommandResult Script(EfCoreCommonOptions options, string fromMigration, string toMigration,
            string output = default, bool? idempotent = default, bool? noTransactions = default)
        {
            throw new System.NotImplementedException();
        }
    }
}