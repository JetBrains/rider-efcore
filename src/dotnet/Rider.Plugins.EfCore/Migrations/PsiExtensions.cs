using System.Collections.Generic;
using System.Linq;
using JetBrains.Application.Progress;
using JetBrains.Metadata.Reader.API;
using JetBrains.ProjectModel;
using JetBrains.ReSharper.Feature.Services.Navigation.Requests;
using JetBrains.ReSharper.Feature.Services.Occurrences;
using JetBrains.ReSharper.Psi;
using JetBrains.ReSharper.Psi.Modules;

namespace Rider.Plugins.EfCore.Migrations
{
  public static class PsiExtensions
  {
    public static IEnumerable<IClass> FindInheritorsOf(this IPsiModule module, IProject project,
      IClrTypeName clrTypeName)
    {
      var psiServices = module.GetPsiServices();

      var symbolScope =
        psiServices.Symbols.GetSymbolScope(module, true,
          true); // caseSensitive should probably come the project language service

      var typeElement = symbolScope.GetTypeElementByCLRName(clrTypeName);

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
  }
}
