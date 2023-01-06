using System.Collections.Generic;
using System.Linq;
using JetBrains.Application.Progress;
using JetBrains.Metadata.Reader.API;
using JetBrains.ReSharper.Feature.Services.Navigation.Requests;
using JetBrains.ReSharper.Feature.Services.Occurrences;
using JetBrains.ReSharper.Psi;
using JetBrains.ReSharper.Psi.Modules;

namespace Rider.Plugins.EfCore.Extensions
{
  public static class PsiExtensions
  {
    public static IAttributeInstance GetAttributeInstance(this IAttributesSet @class, string attributeShortName) =>
      @class
        .GetAttributeInstances(AttributesSource.All)
        .SingleOrDefault(attribute => attribute.GetAttributeShortName() == attributeShortName);

    public static IEnumerable<IClass> FindInheritorsOf(this IPsiModule module, IClrTypeName clrTypeName, bool transitive = true)
    {
      var psiServices = module.GetPsiServices();
      var symbolScope = psiServices.Symbols.GetSymbolScope(module, transitive, true);
      var typeElement = symbolScope.GetTypeElementByCLRName(clrTypeName);

      if (typeElement == null)
        return Enumerable.Empty<IClass>();

      var consumer = new SearchResultsConsumer();
      var pi = NullProgressIndicator.Create();

      psiServices.Finder.FindInheritors(typeElement, symbolScope, consumer, pi);

      return consumer
        .GetOccurrences()
        .OfType<DeclaredElementOccurrence>()
        .Select(occurence => occurence.GetDeclaredElement())
        .Where(element => element != null)
        .Cast<IClass>();
    }
  }
}
