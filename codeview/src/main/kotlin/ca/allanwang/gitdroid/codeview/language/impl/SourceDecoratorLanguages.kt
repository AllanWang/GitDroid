package ca.allanwang.gitdroid.codeview.language.impl

import ca.allanwang.gitdroid.codeview.language.*
import ca.allanwang.gitdroid.codeview.language.ALL_KEYWORDS
import ca.allanwang.gitdroid.codeview.language.CPP_KEYWORDS
import ca.allanwang.gitdroid.codeview.language.C_TYPES
import ca.allanwang.gitdroid.codeview.language.JAVA_KEYWORDS
import ca.allanwang.gitdroid.codeview.language.SH_KEYWORDS
import ca.allanwang.gitdroid.codeview.language.SourceDecoratorOptions.Companion.FLAG_ENABLED


object DefaultCode : SourceDecoratorLang() {
    override fun options(): SourceDecoratorOptions =
        SourceDecoratorOptions(
            keywords = ALL_KEYWORDS.split(","),
            hashComments = FLAG_ENABLED,
            cStyleComments = true,
            multiLineStrings = true,
            regexLiterals = FLAG_ENABLED
        )

    override fun fileExtensions(): Set<String> = setOf("default-code")
}

object CLang : SourceDecoratorLang() {
    override fun options(): SourceDecoratorOptions =
        SourceDecoratorOptions(
            keywords = CPP_KEYWORDS.split(","),
            hashComments = FLAG_ENABLED,
            cStyleComments = true,
            types = C_TYPES
        )

    override fun fileExtensions(): Set<String> = setOf("c", "cc", "cpp", "cxx", "cyc", "m")
}

object JsonLang : SourceDecoratorLang() {
    override fun options(): SourceDecoratorOptions =
        SourceDecoratorOptions(
            keywords = listOf("null", "true", "false")
        )

    override fun fileExtensions(): Set<String> = setOf("json")
}

object CsLang

object JavaLang : SourceDecoratorLang() {
    override fun options(): SourceDecoratorOptions =
        SourceDecoratorOptions(
            keywords = JAVA_KEYWORDS.split(","),
            cStyleComments = true
        )

    override fun fileExtensions(): Set<String> = setOf("java")
}

object BashLang : SourceDecoratorLang() {
    override fun options(): SourceDecoratorOptions =
        SourceDecoratorOptions(
            keywords = SH_KEYWORDS.split(","),
            hashComments = FLAG_ENABLED,
            multiLineStrings = true
        )

    override fun fileExtensions(): Set<String> = setOf("bash", "bsh", "csh", "sh")
}

object PythonLang

object PerlLang

object RubyLang

object JavaScriptLang

object CoffeeLang

object RustLang