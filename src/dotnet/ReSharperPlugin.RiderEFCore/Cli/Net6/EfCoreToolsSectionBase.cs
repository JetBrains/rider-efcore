using System;
using System.Text;
using CliWrap;
using CliWrap.Builders;
using JetBrains.Util;
using ReSharperPlugin.RiderEfCore.Cli.Net6.Models;

namespace ReSharperPlugin.RiderEfCore.Cli.Net6
{
    public abstract class EfCoreToolsSectionBase
    {
        protected readonly ILogger Logger;

        public EfCoreToolsSectionBase(ILogger logger)
        {
            Logger = logger;
        }

        protected EfCoreCommandResult ExecuteLogged(Command command)
        {
            Logger.Info("Executing CLI command: {0}", command);

            var sb = new StringBuilder();

            command |= sb;
            command |= output => Logger.Info($"dotnet ef: {output}");

            var commandResult = command.ExecuteAsync().ConfigureAwait(false).GetAwaiter().GetResult();

            Logger.Info("Exit code: {0}", commandResult.ExitCode);

            return new EfCoreCommandResult(commandResult.ExitCode, sb.ToString());
        }

        // TODO: Add other common options
        protected Command CreateEfCoreCommand(
            EfCoreCommandName commandName,
            EfCoreCommonOptions options,
            Action<ArgumentsBuilder> argsBuilder = null) =>

            CliWrap.Cli.Wrap("dotnet")
                .WithArguments(args =>
                {
                    args
                        .Add("ef")
                        .Add(commandName.CommandParts)
                        .Add("--project").Add(options.MigrationsProject)
                        .Add("--startup-project").Add(options.StartupProject);
                    argsBuilder?.Invoke(args);
                }).WithValidation(CommandResultValidation.ZeroExitCode);
    }
}