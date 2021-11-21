namespace ReSharperPlugin.RiderEfCore.Cli.Net6.Abstractions
{
    public interface IEfCoreToolsClient
    {
        IMigrationsClient Migrations { get; }
        IDatabaseClient Database { get; }
        IDbContextClient DbContext { get; }
    }
}