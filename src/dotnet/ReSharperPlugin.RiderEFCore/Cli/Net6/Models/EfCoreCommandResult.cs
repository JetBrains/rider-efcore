using JetBrains.Annotations;

namespace ReSharperPlugin.RiderEfCore.Cli.Net6.Models
{
    public class EfCoreCommandResult
    {
        public int ExitCode { get; }

        [CanBeNull]
        public string Output { get; }

        public EfCoreCommandResult(int exitCode, [CanBeNull] string output)
        {
            ExitCode = exitCode;
            Output = output;
        }
    }
}