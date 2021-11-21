using System;
using CliWrap;
using CliWrap.Builders;

namespace ReSharperPlugin.RiderEfCore.Cli
{
    public static class EfCoreCommandFactory
    {
        public static Command CreateCommand(EfCoreCommandName commandName, Action<ArgumentsBuilder> argsBuilder = null)
        {
            return CliWrap.Cli.Wrap("dotnet")
                .WithArguments(args =>
                {
                    args.Add("ef").Add(commandName.CommandParts);
                    argsBuilder?.Invoke(args);
                }).WithValidation(CommandResultValidation.ZeroExitCode);
        }
    }
}