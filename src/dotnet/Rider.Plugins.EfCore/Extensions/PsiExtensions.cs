using System.Collections.Generic;
using System.Linq;
using JetBrains.Application.Progress;
using JetBrains.Metadata.Reader.API;
using JetBrains.ProjectModel;
using JetBrains.ReSharper.Feature.Services.Navigation.Requests;
using JetBrains.ReSharper.Feature.Services.Occurrences;
using JetBrains.ReSharper.Psi;
using JetBrains.ReSharper.Psi.Modules;
using Rider.Plugins.EfCore.Rd;

namespace Rider.Plugins.EfCore.Extensions
{
    public static class PsiExtensions
    {
        public static MigrationInfo ToMigrationInfo(this IClass @class)
        {
            var migrationShortName = @class.ShortName;
            var migrationAttribute = @class.GetAttributeInstance("MigrationAttribute");
            var dbContextAttribute = @class.GetAttributeInstance("DbContextAttribute");

            var migrationLongName = migrationAttribute.PositionParameter(0).ConstantValue.Value as string;
            var migrationLongNameWithExtension = $"{migrationLongName}.cs";

            var dbContextClass = dbContextAttribute.PositionParameter(0).TypeValue?.GetScalarType()?
                .GetClrName();
            
            if (migrationLongName is null || dbContextClass is null)
            {
                return null;
            }
            
            var migrationFolderAbsPath = @class.GetClassFolderAbsPath(migrationLongNameWithExtension);

            return new MigrationInfo(
                dbContextClass.FullName, 
                migrationShortName, 
                migrationLongName, 
                migrationFolderAbsPath);
        }

        public static IEnumerable<IClass> FindInheritorsOf(this IPsiModule module, IProject project, IClrTypeName clrTypeName)
        {
            var psiServices = module.GetPsiServices();
            var symbolScope = psiServices.Symbols.GetSymbolScope(module, true, true); // caseSensitive should probably come the project language service
            var typeElement = symbolScope.GetTypeElementByCLRName(clrTypeName);

            var a = symbolScope.GetTypeElementsByCLRName(EfCoreKnownTypeNames.MigrationBaseClass);

            if (typeElement == null)
            {
                return Enumerable.Empty<IClass>();
            }

            var consumer = new SearchResultsConsumer();
            var pi = NullProgressIndicator.Create();

            psiServices.Finder.FindInheritors(typeElement, symbolScope, consumer, pi);

            return consumer
                .GetOccurrences()
                .OfType<DeclaredElementOccurrence>()
                .Select(occurence => occurence.GetDeclaredElement())
                .Where(element => element != null)
                .Where(element => element.GetSourceFiles().All(file => Equals(file.GetProject(), project)))
                .Cast<IClass>();
        }

        public static IAttributeInstance GetAttributeInstance(this IClass @class, string attributeShortName) =>
            @class
                .GetAttributeInstances(AttributesSource.All)
                .SingleOrDefault(attribute => attribute.GetAttributeShortName() == attributeShortName);

        private static string GetClassFolderAbsPath(this IClass @class, string className)
        {
            var result = @class.GetSourceFiles()
                .FirstOrDefault(x => x.Name == className);

            var folder = result?.GetLocation().Directory.FileAccessPath;

            return folder;
        }
    }
}