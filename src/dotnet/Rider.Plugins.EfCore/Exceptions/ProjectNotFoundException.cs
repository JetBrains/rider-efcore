using System;

namespace Rider.Plugins.EfCore.Exceptions
{
    public class ProjectNotFoundException : Exception
    {
        public ProjectNotFoundException(string projectName) : base($"Project with name {projectName} not found")
        {
        }
    }
}