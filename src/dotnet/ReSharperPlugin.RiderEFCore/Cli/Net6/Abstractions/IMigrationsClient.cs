using JetBrains.Annotations;
using ReSharperPlugin.RiderEfCore.Cli.Net6.Models;

namespace ReSharperPlugin.RiderEfCore.Cli.Net6.Abstractions
{
    public interface IMigrationsClient
    {
        EfCoreCommandResult Add(EfCoreCommonOptions options, [CanBeNull] string migrationName,
            [CanBeNull] string outputDirectory = default, [CanBeNull] string @namespace = default);
        EfCoreCommandResult Bundle(EfCoreCommonOptions options, [CanBeNull] string output = default, bool? force = default,
            bool? selfContained = default, [CanBeNull] string targetRuntime = default);
        EfCoreCommandResult List(EfCoreCommonOptions options, [CanBeNull] string connectionString = default, bool? noConnect = default);
        EfCoreCommandResult Remove(EfCoreCommonOptions options, bool? force = default);
        EfCoreCommandResult Script(EfCoreCommonOptions options, string fromMigration, string toMigration,
            [CanBeNull] string output = default, bool? idempotent = default, bool? noTransactions = default);
    }
}