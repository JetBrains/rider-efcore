using CliWrap.Builders;
using JetBrains.Annotations;

namespace ReSharperPlugin.RiderEfCore.Cli.Extensions
{
    public static class ArgumentsBuilderExtensions
    {
        public static ArgumentsBuilder AddOptional(this ArgumentsBuilder builder, string key, [CanBeNull] string value) =>
            value != null
                ? builder.Add(key).Add(value)
                : builder;

        public static ArgumentsBuilder AddOptionalKey(this ArgumentsBuilder builder, string key, bool? value) =>
            value != null
                ? builder.Add(key)
                : builder;
    }
}