using JetBrains.Metadata.Reader.API;
using JetBrains.Metadata.Reader.Impl;

namespace Rider.Plugins.EfCore
{
    public static class EfCoreKnownTypeNames
    {
        public static readonly IClrTypeName MigrationBaseClass = new ClrTypeName("Microsoft.EntityFrameworkCore.Migrations.Migration");
        public static readonly IClrTypeName DbContextBaseClass = new ClrTypeName("Microsoft.EntityFrameworkCore.DbContext");
    }
}