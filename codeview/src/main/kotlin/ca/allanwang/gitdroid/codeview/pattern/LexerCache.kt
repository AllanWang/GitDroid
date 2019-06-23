package ca.allanwang.gitdroid.codeview.pattern

import ca.allanwang.gitdroid.codeview.language.CodeLanguage
import ca.allanwang.gitdroid.codeview.language.impl.KotlinLang

class LexerCache(languages: List<CodeLanguage>) {

    private val langMap: Map<String, CodeLanguage>

    init {
        langMap = languages.flatMap { lang -> lang.fileExtensions().map { it to lang } }.toMap()
    }

    private val lexerMap: MutableMap<CodeLanguage, Lexer> = mutableMapOf()

    fun clear() {
        lexerMap.clear()
    }

    fun getLanguage(content: String, extension: String): CodeLanguage {
        val lang = langMap[extension]
        if (lang != null) {
            return lang
        }
        // TODO add actual fallback
        return KotlinLang
    }

    fun getLexer(content: String, extension: String): Lexer {
        val lang = getLanguage(content, extension)
        return lexerMap.getOrPut(lang) {
            Lexer(lang)
        }
    }

}