using System;

namespace Rider.Plugins.EfCore.Exceptions
{
  public class ProjectNotFoundException : Exception
  {
    public ProjectNotFoundException(Guid projectId) : base($"Project with ID {projectId} not found")
    {
    }
  }
}
