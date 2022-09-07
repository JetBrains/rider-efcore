using System.Collections.Generic;

namespace Rider.Plugins.EfCore.Extensions
{
  public delegate bool FuncTry<T1, T2>(T1 a, out T2 b);

  public static class LinqExtensions
  {
    /// <summary>
    /// Executes delegate with TrySomething semantics and returns new item from out parameter if method returns true.
    /// </summary>
    /// <param name="enumerable">Instance of <see cref="IEnumerable{T1}"/>.</param>
    /// <param name="safeMapping">Mapping with Try semantics.</param>
    /// <typeparam name="T1">Type of a source item.</typeparam>
    /// <typeparam name="T2">Type of a destination item.</typeparam>
    /// <returns>Mapped enumerable.</returns>
    public static IEnumerable<T2> TrySelect<T1, T2>(this IEnumerable<T1> enumerable, FuncTry<T1, T2> safeMapping)
    {
      foreach (var item in enumerable)
      {
        if (safeMapping(item, out var result))
        {
          yield return result;
        }
      }
    }
  }
}
