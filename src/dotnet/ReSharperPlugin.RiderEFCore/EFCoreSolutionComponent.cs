using System;
using System.Collections.Generic;
using System.Linq;
using JetBrains.Core;
using JetBrains.Lifetimes;
using JetBrains.ProjectModel;
using JetBrains.Rd.Tasks;
using JetBrains.RdBackend.Common.Features;
using JetBrains.ReSharper.Resources.Shell;
using JetBrains.Rider.Model;
using JetBrains.Util;
using ReSharperPlugin.RiderEfCore.Cli.Extensions;
using ReSharperPlugin.RiderEfCore.Cli.Net6;
using ReSharperPlugin.RiderEfCore.Cli.Net6.Abstractions;
using ReSharperPlugin.RiderEfCore.Cli.Net6.Models;
using OperationResult = JetBrains.Rider.Model.OperationResult;

namespace ReSharperPlugin.RiderEfCore
{
    [SolutionComponent]
    public class EfCoreSolutionComponent
    {
        private readonly ISolution _solution;
        private readonly IEfCoreToolsClient _toolsClient;

        public EfCoreSolutionComponent(ISolution solution, ILogger logger)
        {
            _solution = solution;
            _toolsClient = EfCoreToolsClientFactory.CreateDefaultClient(logger);

            var riderProjectOutputModel = solution.GetProtocolSolution().GetRiderEfCoreModel();

            riderProjectOutputModel.GetProjectNames.Set(GetProjectNames);

            riderProjectOutputModel.AddMigration.Set(AddMigration);
            riderProjectOutputModel.RemoveLastMigration.Set(RemoveLastMigration);
        }

        private RdTask<List<string>> GetProjectNames(Lifetime lifetime, Unit _)
        {
            using (ReadLockCookie.Create())
            {
                var allProjectNames = EfCoreHelper.GetSupportedMigrationProjects(_solution)
                    .Select(project => project.Name)
                    .ToList();

                return RdTask<List<string>>.Successful(allProjectNames);
            }
        }

        private RdTask<OperationResult> AddMigration(Lifetime lifetime, AddMigrationOptions options)
        {
            using (ReadLockCookie.Create())
            {
                var migrationsProjectFolder = GetProjectPath(options.MigrationsProject);
                var startupProjectFolder = GetProjectPath(options.StartupProject);
                var commonOptions = new EfCoreCommonOptions(migrationsProjectFolder, startupProjectFolder);
                var commandResult = _toolsClient.Migrations.Add(commonOptions, options.MigrationName);

                return MapCommandResult(commandResult);
            }
        }

        private RdTask<OperationResult> RemoveLastMigration(Lifetime lifetime, string projectName)
        {
            using (ReadLockCookie.Create())
            {
                var projectFolder = GetProjectPath(projectName);
                var commonOptions = new EfCoreCommonOptions(projectFolder, projectFolder);
                var commandResult = _toolsClient.Migrations.Remove(commonOptions, force: true);

                return MapCommandResult(commandResult);
            }
        }

        private string GetProjectPath(string projectName)
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

            return project.ProjectFileLocation.Parent.FullPath;
        }

        private static RdTask<OperationResult> MapCommandResult(EfCoreCommandResult commandResult) =>
            commandResult.ExitCode == 0
                ? RdTask<OperationResult>.Successful(commandResult.ToOperationResult())
                : RdTask<OperationResult>.Faulted(
                    new Exception($"Exit code of dotnet ef was not successful: {commandResult.ExitCode}"));
    }
}