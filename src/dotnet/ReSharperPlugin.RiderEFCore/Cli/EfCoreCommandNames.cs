namespace ReSharperPlugin.RiderEfCore.Cli
{
    public static class EfCoreCommandNames
    {
        public static class Database
        {
            public static readonly EfCoreCommandName Update = new EfCoreCommandName(new[] { "database", "update" });
        }

        public static class Migrations
        {
            public static readonly EfCoreCommandName Add = new EfCoreCommandName(new[] { "migrations", "add" });
            public static readonly EfCoreCommandName Remove = new EfCoreCommandName(new[] { "migrations", "remove" });
        }
    }
}