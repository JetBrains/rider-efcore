using JetBrains.Rider.Model;
using ReSharperPlugin.RiderEfCore.Cli.Net6.Models;

namespace ReSharperPlugin.RiderEfCore.Cli.Extensions
{
    public static class EfCoreCommandResultExtensions
    {
        public static OperationResult ToOperationResult(this EfCoreCommandResult commandResult) =>
            new OperationResult(
                commandResult.Command,
                commandResult.Succeeded,
                commandResult.ExitCode,
                commandResult.Output ?? string.Empty);
    }
}