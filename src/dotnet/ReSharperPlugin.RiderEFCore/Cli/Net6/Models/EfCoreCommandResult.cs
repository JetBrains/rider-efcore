using JetBrains.Annotations;

namespace ReSharperPlugin.RiderEfCore.Cli.Net6.Models
{
    public class EfCoreCommandResult
    {
        [NotNull]
        public string Command { get; }

        public bool Succeeded { get; }

        public int ExitCode { get; }

        [CanBeNull]
        public string Output { get; }

        public EfCoreCommandResult([NotNull] string command, bool succeeded, int exitCode, [CanBeNull] string output)
        {
            Command = command;
            Succeeded = succeeded;
            ExitCode = exitCode;
            Output = output;
        }
    }
}