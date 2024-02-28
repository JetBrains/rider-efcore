using System;
using System.Collections.Generic;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata;
using EFCoreConsoleApp2.DAL.Migrations345432234;

namespace EFCoreConsoleApp2.DAL.Contextasdas
{
    public partial class MyDbContextassd : DbContext
    {
        public MyDbContextassd()
        {
        }

        public MyDbContextassd(DbContextOptions<MyDbContextassd> options)
            : base(options)
        {
        }

        public virtual DbSet<Sample1> Sample1s { get; set; }
        public virtual DbSet<Sample2> Sample2s { get; set; }

        protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
        {
            if (!optionsBuilder.IsConfigured)
            {
#warning To protect potentially sensitive information in your connection string, you should move it out of source code. You can avoid scaffolding the connection string by using the Name= syntax to read it from configuration - see https://go.microsoft.com/fwlink/?linkid=2131148. For more guidance on storing connection strings, see http://go.microsoft.com/fwlink/?LinkId=723263.
                optionsBuilder.UseSqlite("Data Source=/Users/seclerp/blogging.db");
            }
        }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            modelBuilder.Entity<Sample1>(entity =>
            {
                entity.HasNoKey();

                entity.ToTable("sample_1");

                entity.Property(e => e.Lolo).HasColumnName("lolo");
            });

            modelBuilder.Entity<Sample2>(entity =>
            {
                entity.HasNoKey();

                entity.ToTable("sample_2");

                entity.Property(e => e.Keke).HasColumnName("keke");
            });

            OnModelCreatingPartial(modelBuilder);
        }

        partial void OnModelCreatingPartial(ModelBuilder modelBuilder);
    }
}
