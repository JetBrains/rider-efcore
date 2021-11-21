namespace ReSharperPlugin.RiderEfCore.Cli
{
    public sealed class EfCoreCommandName
    {
        public string[] CommandParts { get; }

        public EfCoreCommandName(string[] commandParts)
        {
            CommandParts = commandParts;
        }
    }
}