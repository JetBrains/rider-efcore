using JetBrains.Util;
using ReSharperPlugin.RiderEfCore.Cli.Net6.Abstractions;
using ReSharperPlugin.RiderEfCore.Cli.Net6.Models;

namespace ReSharperPlugin.RiderEfCore.Cli.Net6
{
    public class DbContextClient : EfCoreToolsSectionBase, IDbContextClient
    {
        public DbContextClient(ILogger logger) : base(logger)
        {
        }

        public EfCoreCommandResult Info(EfCoreCommonOptions options)
        {
            throw new System.NotImplementedException();
        }

        public EfCoreCommandResult List(EfCoreCommonOptions options)
        {
            throw new System.NotImplementedException();
        }

        public EfCoreCommandResult Optimize(EfCoreCommonOptions options, string outputDirectory = default,
            string @namespace = default)
        {
            throw new System.NotImplementedException();
        }

        public EfCoreCommandResult Scaffold(EfCoreCommonOptions options, EfCoreScaffoldOptions scaffoldOptions)
        {
            throw new System.NotImplementedException();
        }

        public EfCoreCommandResult Script(EfCoreCommonOptions options, string outputFile = default)
        {
            throw new System.NotImplementedException();
        }
    }
}