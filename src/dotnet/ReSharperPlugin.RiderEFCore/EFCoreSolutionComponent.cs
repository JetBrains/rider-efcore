using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using CliWrap;
using JetBrains.Core;
using JetBrains.Lifetimes;
using JetBrains.ProjectModel;
using JetBrains.Rd.Tasks;
using JetBrains.RdBackend.Common.Features;
using JetBrains.Rider.Model;
using JetBrains.Util;

namespace ReSharperPlugin.RiderEfCore
{
    [SolutionComponent]
    // ReSharper disable once InconsistentNaming
    public class EFCoreSolutionComponent
    {
        private readonly ISolution _solution;
        private readonly ILogger _logger;

        public EFCoreSolutionComponent(ISolution solution, ILogger logger)
        {
            _solution = solution;
            _logger = logger;

            var riderProjectOutputModel = solution.GetProtocolSolution().GetRiderEfCoreModel();
            riderProjectOutputModel.GetProjectNames.Set(GetProjectNames);
            riderProjectOutputModel.RemoveLastMigration.Set(RemoveLastMigration);
        }

        private RdTask<List<string>> GetProjectNames(Lifetime lifetime, Unit _)
        {
            var allProjectNames = _solution
                .GetAllProjects()
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

            var command = Cli.Wrap("dotnet").WithArguments(args => args
                .Add(new[] { "ef", "migrations", "remove" })
                .Add("--project").Add(projectFolder))
                .WithValidation(CommandResultValidation.ZeroExitCode)
                | (output => _logger.Info($"dotnet ef: {output}"));

            _logger.Info("Executing CLI command: {0}", command);

            // TODO: How to async?
            var commandResult = command.ExecuteAsync().ConfigureAwait(false).GetAwaiter().GetResult();

            _logger.Info("Exit code: {0}", commandResult.ExitCode);

            return commandResult.ExitCode == 0
                ? RdTask<Unit>.Successful(Unit.Instance)
                : RdTask<Unit>.Faulted(
                    new Exception($"Exit code of dotnet ef was not successful: {commandResult.ExitCode}"));
        }
    }
}