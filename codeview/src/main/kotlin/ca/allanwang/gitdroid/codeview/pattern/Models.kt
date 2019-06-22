package ca.allanwang.gitdroid.codeview.pattern

import ca.allanwang.gitdroid.codeview.highlighter.PR
import ca.allanwang.gitdroid.codeview.language.CodeLanguage
import java.util.regex.Pattern

data class CodePattern(val pr: PR, val pattern: Pattern, val shortcut: String? = null)

data class Decoration(val pos: Int, val pr: PR)

data class LexerJob(val basePos: Int, val source: String)

data class LexerOptions(
    val regexLiterals: Boolean = true,
    val tripeQuotedStrings: Boolean = false,
    val multiLineStrings: Boolean = false,
    val verbatimStrings: Boolean = false
)