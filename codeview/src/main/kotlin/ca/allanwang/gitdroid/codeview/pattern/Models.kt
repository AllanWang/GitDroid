package ca.allanwang.gitdroid.codeview.pattern

import ca.allanwang.gitdroid.codeview.highlighter.PR
import java.util.regex.Pattern

data class CodePattern(val pr: PR, val pattern: Pattern, val shortcut: String? = null)

internal data class Decoration(val pos: Int, val pr: PR)

internal data class Job(val basePos: Int, val source: String, val decorations: List<Decoration>)