using JetBrains.Annotations;
using ReSharperPlugin.RiderEfCore.Cli.Net6.Models;

namespace ReSharperPlugin.RiderEfCore.Cli.Net6.Abstractions
{
    public interface IDbContextClient
    {
        EfCoreCommandResult Info(EfCoreCommonOptions options);
        EfCoreCommandResult List(EfCoreCommonOptions options);
        EfCoreCommandResult Optimize(EfCoreCommonOptions options, [CanBeNull] string outputDirectory = default, [CanBeNull] string @namespace = default);
        EfCoreCommandResult Scaffold(EfCoreCommonOptions options, EfCoreScaffoldOptions scaffoldOptions);
        EfCoreCommandResult Script(EfCoreCommonOptions options, [CanBeNull] string outputFile = default);
    }
}