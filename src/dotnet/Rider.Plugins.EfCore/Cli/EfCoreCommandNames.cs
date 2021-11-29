using System;

namespace Rider.Plugins.EfCore.Cli
{
    [Obsolete("Remove when all commands will be implemented on frontend")]
    public static class EfCoreCommandNames
    {
        [Obsolete]
        public static class Database
        {
            public static readonly EfCoreCommandName Update = new EfCoreCommandName(new[] { "database", "update" });
            public static readonly EfCoreCommandName Drop = new EfCoreCommandName(new[] { "database", "drop" });
        }

        [Obsolete]
        public static class Migrations
        {
            public static readonly EfCoreCommandName Add = new EfCoreCommandName(new[] { "migrations", "add" });
            public static readonly EfCoreCommandName Remove = new EfCoreCommandName(new[] { "migrations", "remove" });
            public static readonly EfCoreCommandName Bundle = new EfCoreCommandName(new[] { "migrations", "bundle" });
            public static readonly EfCoreCommandName List = new EfCoreCommandName(new[] { "migrations", "list" });
            public static readonly EfCoreCommandName Script = new EfCoreCommandName(new[] { "migrations", "script" });
        }

        [Obsolete]
        public static class DbContext
        {
            public static readonly EfCoreCommandName Info = new EfCoreCommandName(new[] { "dbcontext", "info" });
            public static readonly EfCoreCommandName List = new EfCoreCommandName(new[] { "dbcontext", "list" });
            public static readonly EfCoreCommandName Optimize = new EfCoreCommandName(new[] { "dbcontext", "optimize" });
            public static readonly EfCoreCommandName Scaffold = new EfCoreCommandName(new[] { "dbcontext", "scaffold" });
            public static readonly EfCoreCommandName Script = new EfCoreCommandName(new[] { "dbcontext", "script" });
        }
    }
}