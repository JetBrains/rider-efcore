using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace EFCoreConsoleApp2.DAL.Migrations345432234
{
    /// <inheritdoc />
    public partial class Initial : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateTable(
                name: "sample_1",
                columns: table => new
                {
                    lolo = table.Column<long>(type: "INTEGER", nullable: true)
                },
                constraints: table =>
                {
                });

            migrationBuilder.CreateTable(
                name: "sample_2",
                columns: table => new
                {
                    keke = table.Column<long>(type: "INTEGER", nullable: true)
                },
                constraints: table =>
                {
                });
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "sample_1");

            migrationBuilder.DropTable(
                name: "sample_2");
        }
    }
}
