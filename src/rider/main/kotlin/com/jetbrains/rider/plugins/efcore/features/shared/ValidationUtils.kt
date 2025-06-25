package com.jetbrains.rider.plugins.efcore.features.shared

import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.xml.util.XmlStringUtil
import com.jetbrains.rider.plugins.efcore.EfCoreUiBundle
import java.nio.file.Path
import java.util.*

data class ValidationResult(val isValid: Boolean, val errorMessage: String? = null) {
    companion object {
        val VALID = ValidationResult(true)
        fun invalid(errorMessage: String) = ValidationResult(false, errorMessage)
    }
}

/**
 * Based on [com.jetbrains.rider.projectView.actions.newFile.RiderInputValidator.checkInput]
 */
fun validateRelativeFolderPath(inputString: String, parentPath: String) : ValidationResult {
    if (inputString.isEmpty())
        return ValidationResult.VALID

    if (inputString.startsWith("\\") || inputString.startsWith("/"))
        return ValidationResult(false, EfCoreUiBundle.message(
            "relative.folder.path.validation.error.name.starts.with.0", inputString.first()))

    if (SystemInfo.isWindows) {
        val invalidCharacters = Regex("[<>\"*?|]")
            .findAll(inputString)
            .map { it.value }
            .map { XmlStringUtil.escapeString(it) }
            .distinct()
        if (invalidCharacters.any())
            return ValidationResult.invalid(EfCoreUiBundle.message(
                "relative.folder.path.validation.error.name.contains.invalid.characters.0",
                invalidCharacters.joinToString(" ")))
    }

    val tokenizer = StringTokenizer(inputString, "\\/")
    var vFile: VirtualFile? = VfsUtil.findFile(Path.of(parentPath), true)
    while (tokenizer.hasMoreTokens()) {
        val token = tokenizer.nextToken()
        if (vFile != null) {
            if (".." == token) {
                vFile = vFile.parent
                if (vFile == null)
                    return ValidationResult.invalid(EfCoreUiBundle.message(
                        "relative.folder.path.validation.error.not.a.valid.directory"))
            } else if ("." != token) {
                val child = vFile.findChild(token)
                if (child != null && !child.isDirectory)
                    return ValidationResult.invalid(EfCoreUiBundle.message(
                        "relative.folder.path.validation.error.contains.invalid.directory", token))
                vFile = child
            }
        }
    }

    return ValidationResult.VALID
}