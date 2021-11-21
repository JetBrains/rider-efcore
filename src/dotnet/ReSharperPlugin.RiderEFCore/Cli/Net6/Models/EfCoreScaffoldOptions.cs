using JetBrains.Annotations;

namespace ReSharperPlugin.RiderEfCore.Cli.Net6.Models
{
    public class EfCoreScaffoldOptions
    {
        public string ConnectionString { get; }
        public string ProviderName { get; }
        public bool? DataAnnotations { get; }
        public string Context { get; }
        public string ContextDir { get; }
        public string ContextNamespace { get; }
        public bool? Force { get; }
        public string OutputDirectory { get; }
        public string Namespace { get; }
        public string Schema { get; }
        public string TableName { get; }
        public bool? UseDatabaseNames { get; }
        public bool? NoOnConfiguring { get; }
        public bool? NoPluralize { get; }

        public EfCoreScaffoldOptions([NotNull] string connectionString, [NotNull] string providerName,
            bool? dataAnnotations = default, string context = default, string contextDir = default,
            string contextNamespace = default, bool? force = default, string outputDirectory = default,
            string @namespace = default, string schema = default, string tableName = default,
            bool? useDatabaseNames = default, bool? noOnConfiguring = default, bool? noPluralize = default)
        {
            ConnectionString = connectionString;
            ProviderName = providerName;
            DataAnnotations = dataAnnotations;
            Context = context;
            ContextDir = contextDir;
            ContextNamespace = contextNamespace;
            Force = force;
            OutputDirectory = outputDirectory;
            Namespace = @namespace;
            Schema = schema;
            TableName = tableName;
            UseDatabaseNames = useDatabaseNames;
            NoOnConfiguring = noOnConfiguring;
            NoPluralize = noPluralize;
        }
    }
}