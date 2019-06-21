package ca.allanwang.gitdroid.codeview.highlighter

import ca.allanwang.gitdroid.codeview.pattern.PatternUtil
import java.util.regex.Pattern

interface CodeLanguage {
    val extension: Pattern
    val patterns: List<CodePattern>
}


object KotlinLang : CodeLanguage {
    override val extension: Pattern = "kt".toPattern()
    override val patterns: List<CodePattern> = with(PatternUtil) {
        listOf()
    }
}