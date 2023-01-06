using System;
using System.Collections.Generic;
using System.Linq;
using JetBrains.Core;
using JetBrains.Lifetimes;
using JetBrains.Platform.RdFramework.Impl;
using JetBrains.ProjectModel;
using JetBrains.ProjectModel.NuGet.DotNetTools;
using JetBrains.Rd.Tasks;
using JetBrains.RdBackend.Common.Features;
using JetBrains.ReSharper.Resources.Shell;
using JetBrains.Util;
using Rider.Plugins.EfCore.Compatibility;
using Rider.Plugins.EfCore.DbContext;
using Rider.Plugins.EfCore.Exceptions;
using Rider.Plugins.EfCore.Logging;
using Rider.Plugins.EfCore.Mapping;
using Rider.Plugins.EfCore.Migrations;
using Rider.Plugins.EfCore.Rd;
using Rider.Plugins.EfCore.Tracking;

namespace Rider.Plugins.EfCore
{
  [SolutionComponent]
  public class EfCoreSolutionComponent
  {
    private readonly Lifetime _lifetime;
    private readonly ISolution _solution;
    private readonly ShellRdDispatcher _shellRdDispatcher;
    private readonly SupportedMigrationsProjectsProvider _supportedMigrationsProjectsProvider;
    private readonly SupportedStartupProjectsProvider _supportedStartupProjectsProvider;
    private readonly MigrationsProvider _migrationsProvider;
    private readonly DbContextProvider _dbContextProvider;
    private readonly ILogger _logger;

    private readonly RiderEfCoreModel _efCoreModel;

    private bool _toolsNotInstalledNotified = false;

    public EfCoreSolutionComponent(
      Lifetime lifetime,
      ISolution solution,
      SolutionTracker solutionTracker,
      ShellRdDispatcher shellRdDispatcher,
      SupportedMigrationsProjectsProvider supportedMigrationsProjectsProvider,
      SupportedStartupProjectsProvider supportedStartupProjectsProvider,
      MigrationsProvider migrationsProvider,
      DbContextProvider dbContextProvider,
      ILogger logger)
    {
      _lifetime = lifetime;
      _solution = solution;
      _shellRdDispatcher = shellRdDispatcher;
      _supportedMigrationsProjectsProvider = supportedMigrationsProjectsProvider;
      _supportedStartupProjectsProvider = supportedStartupProjectsProvider;
      _migrationsProvider = migrationsProvider;
      _dbContextProvider = dbContextProvider;
      _logger = logger;

      _efCoreModel = solution.GetProtocolSolution().GetRiderEfCoreModel();

      _efCoreModel.HasAvailableMigrations.Set(HasAvailableMigrations);
      _efCoreModel.GetAvailableMigrations.Set(GetAvailableMigrations);
      _efCoreModel.GetAvailableDbContexts.Set(GetAvailableDbContexts);

      solutionTracker.OnAfterSolutionUpdate += InvalidateProjects;
      solutionTracker.OnAfterToolsCacheUpdate += InvalidateEfToolsDefinition;
      solutionTracker.OnAfterNuGetUpdate += InvalidateProjects;
      solutionTracker.OnAfterSolutionLoad += OnSolutionLoaded;

      solutionTracker.Setup();
    }

    //
    // Listener handlers implementation

    private void OnSolutionLoaded()
    {
      InvalidateProjects();

      if (_efCoreModel.EfToolsDefinition.Maybe.HasValue)
      {
        var efToolsDefinitionValue = _efCoreModel.EfToolsDefinition.Value;
        if (efToolsDefinitionValue != null)
        {
          CheckToolsInstalled(efToolsDefinitionValue);
        }
      }

      _efCoreModel.EfToolsDefinition.Advise(_lifetime, CheckToolsInstalled);
    }

    private void CheckToolsInstalled(EfToolDefinition efToolsDefinition)
    {
      _shellRdDispatcher.Queue(() =>
      {
        var startupProjects = _efCoreModel.AvailableStartupProjects.Value;

        if (!_toolsNotInstalledNotified && startupProjects.Count > 0 && efToolsDefinition.ToolKind == ToolKind.None)
        {
          _toolsNotInstalledNotified = true;
          _efCoreModel.OnMissingEfCoreToolsDetected.Start(_lifetime, Unit.Instance);
        }
      });
    }

    private void InvalidateEfToolsDefinition(DotNetToolCache cache)
    {
      var allLocalTools = cache.ToolLocalCache.GetAllLocalTools();

      if (allLocalTools is null) return;

      var dotnetEfLocalTool = allLocalTools.FirstOrDefault(tool => tool.PackageId == KnownDotnetTools.EfCoreTools);

      var toolKind = ToolKind.None;
      var version = string.Empty;

      if (dotnetEfLocalTool is { })
      {
        toolKind = ToolKind.Local;
        version = dotnetEfLocalTool.Version;
      }
      else
      {
        var dotnetEfGlobalTool = cache.ToolGlobalCache.GetGlobalTool(KnownDotnetTools.EfCoreTools);

        if (dotnetEfGlobalTool is { Count: 1 })
        {
          toolKind = ToolKind.Global;
          version = dotnetEfGlobalTool[0].Version.ToString();
        }
      }

      _efCoreModel.EfToolsDefinition.Value = new EfToolDefinition(version, toolKind);
    }

    private void InvalidateProjects()
    {
      InvalidateStartupProjects();
      InvalidateMigrationsProjects();
    }

    private void InvalidateStartupProjects()
    {
      using var cookie = ReadLockCookie.Create();

      var allProjectNames = _supportedStartupProjectsProvider
        .GetSupportedStartupProjects()
        .Select(project => project.ToStartupProjectInfo())
        .ToList();

      _shellRdDispatcher.Queue(() =>
      {
        _efCoreModel.AvailableStartupProjects.Value = allProjectNames;

        _logger.LogFlow($"{nameof(EfCoreSolutionComponent)}.{nameof(InvalidateStartupProjects)}",
          "Startup projects invalidated:" +
          $"\n\t{string.Join("\n\t", _efCoreModel.AvailableStartupProjects.Value.Select(project => project.Name))}");
      });
    }

    private void InvalidateMigrationsProjects()
    {
      using var cookie = ReadLockCookie.Create();

      var allProjectNames = _supportedMigrationsProjectsProvider
        .GetSupportedMigrationProjects()
        .Select(project => project.ToMigrationsProjectInfo())
        .ToList();

      _shellRdDispatcher.Queue(() =>
      {
        _efCoreModel.AvailableMigrationProjects.Value = allProjectNames;

        _logger.LogFlow($"{nameof(EfCoreSolutionComponent)}.{nameof(InvalidateMigrationsProjects)}",
          "Migration projects invalidated:" +
          $"\n\t{string.Join("\n\t", _efCoreModel.AvailableMigrationProjects.Value.Select(project => project.Name))}");
      });
    }

    //
    // Calls implementations

    private RdTask<bool> HasAvailableMigrations(Lifetime lifetime, MigrationsIdentity identity)
    {
      using (ReadLockCookie.Create())
      {
        var project = _solution.GetProjectByGuid(identity.ProjectId);
        if (project is null)
        {
          return RdTask<bool>.Faulted(new ProjectNotFoundException(identity.ProjectId));
        }

        var hasMigrations = _migrationsProvider.HasMigrations(project, identity.DbContextClassFullName);

        return RdTask<bool>.Successful(hasMigrations);
      }
    }

    private RdTask<List<MigrationInfo>> GetAvailableMigrations(Lifetime lifetime, MigrationsIdentity identity)
    {
      using (ReadLockCookie.Create())
      {
        var project = _solution.GetProjectByGuid(identity.ProjectId);

        if (project is null)
        {
          return RdTask<List<MigrationInfo>>.Faulted(new ProjectNotFoundException(identity.ProjectId));
        }

        var foundMigrations = _migrationsProvider.GetMigrations(project, identity.DbContextClassFullName).ToList();

        return RdTask<List<MigrationInfo>>.Successful(foundMigrations);
      }
    }

    private RdTask<List<DbContextInfo>> GetAvailableDbContexts(Lifetime lifetime, Guid projectId)
    {
      using (ReadLockCookie.Create())
      {
        var project = _solution.GetProjectByGuid(projectId);

        if (project is null)
        {
          return RdTask<List<DbContextInfo>>.Faulted(new ProjectNotFoundException(projectId));
        }

        var foundDbContexts = _dbContextProvider.GetDbContexts(project).ToList();

        return RdTask<List<DbContextInfo>>.Successful(foundDbContexts);
      }
    }
  }
}
