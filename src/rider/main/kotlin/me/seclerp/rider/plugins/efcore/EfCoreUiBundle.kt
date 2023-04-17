package me.seclerp.rider.plugins.efcore

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier

private const val BUNDLE = "messages.EfCoreUiBundle"

object EfCoreUiBundle : DynamicBundle(BUNDLE) {

    @Nls
    fun message(
        @PropertyKey(resourceBundle = BUNDLE) key: String,
        vararg params: Any
    ): String {
        return getMessage(key, *params)
    }

    fun messagePointer(
        @PropertyKey(resourceBundle = BUNDLE) key: String,
        vararg params: Any
    ): Supplier<@Nls String> {
        return getLazyMessage(key, *params)
    }
}