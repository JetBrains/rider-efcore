using System;
using System.Collections.Generic;
using System.Linq;
using JetBrains.Core;
using JetBrains.Lifetimes;
using JetBrains.ProjectModel;
using JetBrains.Rd.Tasks;
using JetBrains.RdBackend.Common.Features;
using JetBrains.Rider.Model;
using JetBrains.Util;
using ReSharperPlugin.RiderEfCore.Cli;
using ReSharperPlugin.RiderEfCore.Cli.Net6;
using ReSharperPlugin.RiderEfCore.Cli.Net6.Abstractions;
using ReSharperPlugin.RiderEfCore.Cli.Net6.Models;

namespace ReSharperPlugin.RiderEfCore
{
    [SolutionComponent]
    public class EfCoreSolutionComponent
    {
        private readonly ISolution _solution;
        private readonly ILogger _logger;
        private readonly IEfCoreToolsClient _toolsClient;

        public EfCoreSolutionComponent(ISolution solution, ILogger logger)
        {
            _solution = solution;
            _logger = logger;
            _toolsClient = EfCoreToolsClientFactory.CreateDefaultClient(_logger);

            var riderProjectOutputModel = solution.GetProtocolSolution().GetRiderEfCoreModel();
            riderProjectOutputModel.GetProjectNames.Set(GetProjectNames);
            riderProjectOutputModel.RemoveLastMigration.Set(RemoveLastMigration);
        }

        private RdTask<List<string>> GetProjectNames(Lifetime lifetime, Unit _)
        {
            var allProjectNames = EfCoreHelper.GetSupportedMigrationProjects(_solution)
                .Select(project => project.Name)
                .ToList();

            return RdTask<List<string>>.Successful(allProjectNames);
        }

        private RdTask<Unit> RemoveLastMigration(Lifetime lifetime, string projectName)
        {
            var project = _solution.GetProjectByName(projectName);
            if (project is null)
            {
                // TODO
                throw new ArgumentException(null, nameof(project));
            }

            if (project.ProjectFile is null)
            {
                // TODO
                throw new ArgumentException(null, nameof(project.ProjectFile));
            }

            if (project.ProjectFile.ParentFolder is null)
            {
                // TODO
                throw new ArgumentException(null, nameof(project.ProjectFile.ParentFolder));
            }

            var projectFolder = project.ProjectFileLocation.Parent.FullPath;
            var commonOptions = new EfCoreCommonOptions(projectFolder, projectFolder);
            // TODO: How to async?
            var commandResult = _toolsClient.Migrations.Remove(commonOptions, force: true);

            return commandResult.ExitCode == 0
                ? RdTask<Unit>.Successful(Unit.Instance)
                : RdTask<Unit>.Faulted(
                    new Exception($"Exit code of dotnet ef was not successful: {commandResult.ExitCode}"));
        }
    }
}