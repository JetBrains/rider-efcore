// Learn more about F# at http://docs.microsoft.com/dotnet/fsharp

open BloggingModel

// Define a function to construct a message to print
let from whom =
    sprintf "from %s" whom

[<EntryPoint>]
let main argv =
    use ctx = new BloggingContext()
    printfn $"Database path: {ctx}."
    let blog: Blog = { Id = 123; Url = "456" }
    ctx.Add blog |> ignore
    ctx.SaveChanges() |> ignore
    let blog = ctx.Set<Blog>().Find(123)
    let blog = { blog with Url = "789" }
    ctx.SaveChanges() |> ignore
    0 // return an integer exit code