package ca.allanwang.gitdroid.codeview.language

import ca.allanwang.gitdroid.codeview.language.impl.KotlinLang
import ca.allanwang.gitdroid.codeview.pattern.CodePattern

interface CodeLanguage {
    fun fileExtensions(): Set<String>
    fun extendedLangs(): Set<CodeLanguage> = emptySet()
    fun shortcutPatterns(): List<CodePattern>
    fun fallthroughPatterns(): List<CodePattern>

    companion object {
        fun all(): List<CodeLanguage> = listOf(KotlinLang)
    }
}