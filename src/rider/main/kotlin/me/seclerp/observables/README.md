## `me.seclerp.observables`

This package provides very basic implementation of Observer pattern implemented in the scope of MVVM and data-binding in Kotlin-based applications.

### `ObservableProperty<T>`

`ObservableProperty<T>` class provides a wrapper over some arbitary typed value that could be subscribed for updates:

```kotlin
val firstName = ObservableProperty<String>("")
```

Each ObservableProperty instance should have a default value passed in the constructor.

> **Note**: values inside `ObservableProperty` are **nullable**.

#### Basic subscribing

To subscribe and do some action after property's update you could call `afterChanged` method:

```kotlin
firstName.afterChange {
    log("First name was changed: ${it}")
}
```

You could also subscribe to changed event directly:

```kotlin
firstName.onChange += someFunction
```

#### Access the value

```kotlin
val nullableValue = firstName.value
val nonNullableValue = firstName.notNullValue // the same as firstName.value!!
```

#### Using bindings

You could bind one `ObservableProperty` onto another. They could also have different types:

```kotlin
val greetings = ObservableProperty("").bind(firstName) {
    "Hello, {it}!"
}
```

Function provided to `bind` would be called only on next `firstName` update. To execute it immediately (to provide correct initial data mapped) you could use optional `warmUp` parameter:

```kotlin
val greetings = ObservableProperty("").bind(warmUp = true, firstName) {
    "Hello, {it}!"
}
```

To obtain two-way binding, just provide 2 functions into `bind` call:

```kotlin
data class FirstNameInfo(val firstName: String)

val firstNameInfo = ObservableProperty(FirstNameInfo("")).bind(firstName)
    { FirstNameInfo(it) }
    { it.firstName }
)
```

If you want to create a property already based on some other property, you could use `map`. Code above could be rewritten to:

```kotlin
data class FirstNameInfo(val firstName: String)

val firstNameInfo = firstName.map(
    { FirstNameInfo(it) }
    { it.firstName }
)
```

### `ObservableCollection<T>`

TODO

### `Event<T>`

This is a thin helper class that works like a delegate in C#. You could add an arbitrary amount of listeners and call them at once. Example:

```kotlin
data class Payload(val data: String)

val onPayloadReceived = Event<Payload>()
onPayloadReceived += { log(it.data) }
onPayloadReceived += { someCall(it) }

onPayloadReceived.invoke(Payload("Hello"))
```