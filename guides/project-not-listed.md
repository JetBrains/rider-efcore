# Why is my project not listed here?
This guide should help you if you don't see your **Startup project** in the list.

### Entity Framework Core UI plugin requirements
- JetBrains Rider **2022.2+**
- EF Core **5.0+** with projects under such target frameworks:
    - `net7.0` (preview)
    - `net6.0`
    - `net5.0`
    - `netcoreapp3.1`
    - `netstandard2.1` (only for Migrations projects)
    - `netstandard2.0` (only for Migrations projects)
- EF Core global tools (`dotnet ef`) 5.0+ installed. [How to install](https://docs.microsoft.com/en-us/ef/core/cli/dotnet#installing-the-tools)
- DatabaseContext class (name can be anything) that **inherits** `DbContext` and **reference** to [Microsoft.EntityFrameworkCore](https://www.nuget.org/packages/Microsoft.EntityFrameworkCore) 
- Project that **contains** reference to migration tool package `Microsoft.EntityFrameworkCore.Design` or `Microsoft.EntityFrameworkCore.Tools` and it will be defined by **plugin** as a `Startup project` (**can be** one project in the all solution)

> **Note**: Projects with older versions of EF Core might work, but with issues

### How to install requirements
- Rider can be installed by [official website](https://www.jetbrains.com/rider/)
- EF Core can be installed by
  - `dotnet add package Microsoft.EntityFrameworkCore`
  - Rider NuGet [package manager](https://www.jetbrains.com/help/rider/Using_NuGet.html)
  - others described in EF Core package [page](https://www.nuget.org/packages/Microsoft.EntityFrameworkCore)
- EF Core global tools can be installed by [guide from Microsoft](https://docs.microsoft.com/en-us/ef/core/cli/dotnet#installing-the-tools)
- DatabaseContext class can be created by [guide from Microsoft](https://docs.microsoft.com/en-us/ef/core/get-started/overview/first-app?tabs=netcore-cli#create-the-model)
- Migration tools in the project that will be Startup project
  - `dotnet add package Microsoft.EntityFrameworkCore.Tools`
  - Rider NuGet [package manager](https://www.jetbrains.com/help/rider/Using_NuGet.html)
  - others described in EF Core Tools package [page](https://www.nuget.org/packages/Microsoft.EntityFrameworkCore.Tools)

### Check up before using plugin
- You have global tools (`dotnet ef`) with right version
- You have project contains `DatabaseContext` class that **inherits** `DbContext`
- You have project which will be `Startup project` by have reference to **migration tool package**

> **Note**: One project can be **Migration project**, **Startup project** and contains **DatabaseContext** class