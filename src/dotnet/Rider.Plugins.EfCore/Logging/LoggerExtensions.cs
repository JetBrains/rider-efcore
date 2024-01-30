using JetBrains.Diagnostics;
using JetBrains.Util;

namespace Rider.Plugins.EfCore.Logging
{
  public static class LoggerExtensions
  {
    public static void LogFlow(this ILogger logger, string source, string message)
    {
      logger.Log(LoggingLevel.VERBOSE, $"[EF Core]: {source} - {message}");
    }
  }
}
