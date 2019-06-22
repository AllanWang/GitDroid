package ca.allanwang.gitdroid.codeview.language

import ca.allanwang.gitdroid.codeview.pattern.CodePattern
import java.util.regex.Pattern

interface CodeLanguage {
    val id: String
    val extension: Pattern
    fun patterns(): List<CodePattern>
}