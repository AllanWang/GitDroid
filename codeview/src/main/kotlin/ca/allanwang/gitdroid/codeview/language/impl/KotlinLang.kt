package ca.allanwang.gitdroid.codeview.language.impl

import ca.allanwang.gitdroid.codeview.highlighter.PR
import ca.allanwang.gitdroid.codeview.language.impl.CodeLanguage
import ca.allanwang.gitdroid.codeview.pattern.CodePattern
import ca.allanwang.gitdroid.codeview.pattern.CodePatternUtil
import ca.allanwang.gitdroid.codeview.pattern.PatternUtil
import java.util.regex.Pattern

/**
 * See
 * https://github.com/google/code-prettify/blob/master/src/lang-kotlin.js
 */
object KotlinLang : CodeLanguage {
    override fun fileExtensions() = setOf("kt", "kotlin")
    override fun shortcutPatterns(): List<CodePattern> = listOf(
        CodePattern(
            PR.Plain,
            Pattern.compile("^[\\t\\n\\r \\xA0]+"),
            "\t\n\r \u00A0"
        ),
        CodePattern(
            PR.Punctuation,
            Pattern.compile("^[.!%&()*+,\\-;<=>?\\[\\\\\\]^{|}:]+"),
            ".!%&()*+,-;<=>?[\\]^{|}:"
        )
    )

    override fun fallthroughPatterns(): List<CodePattern> = with(CodePatternUtil) {
        listOf(
            keywords(
                "package",
                "public",
                "protected",
                "private",
                "open",
                "abstract",
                "constructor",
                "final",
                "override",
                "import",
                "for",
                "while",
                "as",
                "typealias",
                "get",
                "set",
                "((data|enum|annotation|sealed) )?class",
                "this",
                "super",
                "val",
                "var",
                "fun",
                "is",
                "in",
                "throw",
                "return",
                "break",
                "continue",
                "(companion )?object",
                "if",
                "try",
                "else",
                "do",
                "when",
                "init",
                "interface",
                "typeof", blockFront = true
            ),
            CodePattern(
                PR.Literal,
                PatternUtil.keywords("true", "false", "null")
            ),
            // number literals
            CodePattern(
                PR.Literal,
                Pattern.compile("^(0[xX][0-9a-fA-F_]+L?|0[bB][0-1]+L?|[0-9_.]+([eE]-?[0-9]+)?[fFL]?)")
            ),
            CodePattern(
                PR.Type,
                Pattern.compile("^(\\b[A-Z]+[a-z][a-zA-Z0-9_\$@]*|`.*`)")
            ),
            doubleSlashComment(),
            slashStarCommentAndDoc(),
            // char
            CodePattern(
                PR.String,
                Pattern.compile("'.'")
            ),
            // string
            CodePattern(
                PR.String,
                Pattern.compile("^\"([^\"\\\\]|\\\\[\\s\\S])*\"")
            ),
            // multiline string
            CodePattern(
                PR.String,
                Pattern.compile("^\"{3}[\\s\\S]*?[^\\\\]\"{3}")
            ),
            // annotation (and label)
            CodePattern(
                PR.Literal,
                Pattern.compile("^@([a-zA-Z0-9_\$@]*|`.*`)")
            ),
            // label definition
            CodePattern(
                PR.Literal,
                Pattern.compile("^[a-zA-Z0-9_]+@")
            ),
            varName()
        )
    }
}