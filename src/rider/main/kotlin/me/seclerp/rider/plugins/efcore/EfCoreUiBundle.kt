package me.seclerp.rider.plugins.efcore

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier

class EfCoreUiBundle : DynamicBundle(BUNDLE) {
    companion object {
        @NonNls
        private const val BUNDLE = "messages.EfCoreUiBundle"
        private val INSTANCE: EfCoreUiBundle = EfCoreUiBundle()

        @Nls
        fun message(
            @PropertyKey(resourceBundle = BUNDLE) key: String,
            vararg params: Any
        ): String {
            return INSTANCE.getMessage(key, *params)
        }

        fun messagePointer(
            @PropertyKey(resourceBundle = BUNDLE) key: String,
            vararg params: Any
        ): Supplier<String> {
            return INSTANCE.getLazyMessage(key, *params)
        }
    }
}