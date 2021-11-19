using System.Collections.Generic;
using System.Linq;
using JetBrains.Core;
using JetBrains.Lifetimes;
using JetBrains.ProjectModel;
using JetBrains.Rd.Tasks;
using JetBrains.RdBackend.Common.Features;
using JetBrains.Rider.Model;

namespace ReSharperPlugin.RiderEfCore
{
    [SolutionComponent]
    public class EFCoreSolutionComponent
    {
        private readonly ISolution _solution;

        public EFCoreSolutionComponent(ISolution solution)
        {
            _solution = solution;

            var riderProjectOutputModel = solution.GetProtocolSolution().GetRiderEfCoreModel();
            riderProjectOutputModel.GetProjectNames.Set(GetProjectNames);
        }

        private RdTask<List<string>> GetProjectNames(Lifetime lifetime, Unit _)
        {
            var allProjectNames = _solution
                .GetAllProjects()
                .Select(project => project.Name)
                .ToList();

            return RdTask<List<string>>.Successful(allProjectNames);
        }
    }
}