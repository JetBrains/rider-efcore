// using System.Threading;
// using JetBrains.Application.BuildScript.Application.Zones;
// using JetBrains.ReSharper.Feature.Services;
// using JetBrains.ReSharper.Psi.CSharp;
// using JetBrains.ReSharper.TestFramework;
// using JetBrains.TestFramework;
// using JetBrains.TestFramework.Application.Zones;
// using NUnit.Framework;
//
// [assembly: Apartment(ApartmentState.STA)]
//
// namespace Rider.Plugins.EfCore.Tests
// {
//   [ZoneDefinition]
//   public class RiderEfCoreTestEnvironmentZone : ITestsEnvZone, IRequire<PsiFeatureTestZone>, IRequire<IRiderEfCoreZone>
//   {
//   }
//
//   [ZoneMarker]
//   public class ZoneMarker : IRequire<ICodeEditingZone>, IRequire<ILanguageCSharpZone>,
//     IRequire<RiderEfCoreTestEnvironmentZone>
//   {
//   }
//
//   [SetUpFixture]
//   public class RiderEfCoreTestsAssembly : ExtensionTestEnvironmentAssembly<RiderEfCoreTestEnvironmentZone>
//   {
//   }
// }
