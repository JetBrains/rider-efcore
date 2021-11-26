using JetBrains.Metadata.Reader.API;
using JetBrains.Metadata.Reader.Impl;

namespace ReSharperPlugin.RiderEfCore
{
    public static class EfCoreKnownTypeNames
    {
        public static readonly IClrTypeName MigrationBaseClass = new ClrTypeName("Microsoft.EntityFrameworkCore.Migrations.Migration");
        public static readonly IClrTypeName MigrationAttribute = new ClrTypeName("Microsoft.EntityFrameworkCore.Migrations.MigrationAttribute");
    }
}