using JetBrains.Annotations;
using ReSharperPlugin.RiderEfCore.Cli.Net6.Models;

namespace ReSharperPlugin.RiderEfCore.Cli.Net6.Abstractions
{
    public interface IDatabaseClient
    {
        EfCoreCommandResult Drop(EfCoreCommonOptions options, bool? force = default, bool? dryRun = default);
        EfCoreCommandResult Update(EfCoreCommonOptions options, string migration, [CanBeNull] string connectionString = default);
    }
}