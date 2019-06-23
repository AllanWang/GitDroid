package ca.allanwang.gitdroid.codeview.language

import ca.allanwang.gitdroid.codeview.highlighter.PR
import ca.allanwang.gitdroid.codeview.pattern.CodePattern
import ca.allanwang.gitdroid.codeview.pattern.CodePatternUtil
import java.util.regex.Pattern

data class SourceDecoratorOptions(
    // Order matters
    val keywords: List<String> = emptyList(),
    val hashComments: Int = FLAG_DISABLED,
    val cStyleComments: Boolean = false,
    val verbatimStrings: Boolean = false,
    val types: Pattern? = null,
    val multiLineStrings: Boolean = false,
    val tripleQuotedStrings: Boolean = false,
    val regexLiterals: Int = FLAG_DISABLED
) {
    companion object {
        const val FLAG_DISABLED = 0b0
        const val FLAG_ENABLED = 0b1
        const val FLAG_MULTILINE = FLAG_ENABLED and 0b10
    }
}

/**
 * A set of tokens that can precede a regular expression literal in
 * javascript
 * http://web.archive.org/web/20070717142515/http://www.mozilla.org/js/language/js20/rationale/syntax.html
 * has the full list, but I've removed ones that might be problematic when
 * seen in languages that don't support regular expression literals.
 *
 *
 * Specifically, I've removed any keywords that can't precede a regexp
 * literal in a syntactically legal javascript program, and I've removed the
 * "in" keyword since it's not a keyword in many languages, and might be used
 * as a count of inches.
 *
 *
 * The link above does not accurately describe EcmaScript rules since
 * it fails to distinguish between (a=++/b/i) and (a++/b/i) but it works
 * very well in practice.
 */
private const val REGEXP_PRECEDER_PATTERN =
    "(?:^^\\.?|[+-]|[!=]=?=?|\\#|%=?|&&?=?|\\(|\\*=?|[+\\-]=|->|\\/=?|::?|<<?=?|>>?>?=?|,|;|\\?|@|\\[|~|\\{|\\^\\^?=?|\\|\\|?=?|break|case|continue|delete|do|else|finally|instanceof|return|throw|try|typeof)\\s*"


/**
 * Creates a code language using source decorations.
 * See Prettify#sourceDecorator
 */
abstract class SourceDecoratorLang : CodeLanguage {
    abstract fun options(): SourceDecoratorOptions

    final override fun shortcutPatterns(): List<CodePattern> {
        val shortcutPatterns: MutableList<CodePattern> = mutableListOf()
        CodePatternUtil.apply {
            options().apply {
                shortcutPatterns.add(
                    when {
                        tripleQuotedStrings -> tripleQuotedStrings()
                        multiLineStrings -> multiLineStrings()
                        else -> singleLineStrings()
                    }
                )
                if (hashComments != SourceDecoratorOptions.FLAG_DISABLED) {
                    if (cStyleComments) {
                        val pattern: Pattern = if (hashComments == SourceDecoratorOptions.FLAG_MULTILINE) {
                            Pattern.compile("^#(?:##(?:[^#]|#(?!##))*(?:###|\$)|.*)")
                        } else {
                            // Stop C preprocessor declarations at an unclosed open comment
                            Pattern.compile("^#(?:(?:define|e(?:l|nd)if|else|error|ifn?def|include|line|pragma|undef|warning)\\b|[^\r\n]*)")
                        }
                        shortcutPatterns.add(CodePattern(PR.Comment, pattern, "#"))
                    } else {
                        shortcutPatterns.add(CodePattern(PR.Comment, Pattern.compile("^#[^\r\n]*"), "#"))
                    }
                }
                shortcutPatterns.add(CodePattern(PR.Plain, Pattern.compile("^\\s+"), " \r\n\t\\ua0"))
            }
        }
        return shortcutPatterns
    }

    /**
     * Matches Prettify#sourceDecorator
     */
    final override fun fallthroughPatterns(): List<CodePattern> {
        val fallthroughPatterns: MutableList<CodePattern> = mutableListOf()
        CodePatternUtil.apply {
            options().apply {
                if (verbatimStrings) {
                    fallthroughPatterns.add(
                        CodePattern(
                            PR.String,
                            Pattern.compile("^@\"(?:[^\"]|\"\")*(?:\"|$)")
                        )
                    )
                }
                if (cStyleComments) {
                    if (hashComments != SourceDecoratorOptions.FLAG_DISABLED) {
                        // #include <stdio.h>
                        fallthroughPatterns.add(
                            CodePattern(
                                PR.String,
                                Pattern.compile("^<(?:(?:(?:\\.\\.\\/)*|\\/?)(?:[\\w-]+(?:\\/[\\w-]+)+)?[\\w-]+\\.h(?:h|pp|\\+\\+)?|[a-z]\\w*)>")
                            )
                        )
                    }
                    fallthroughPatterns.add(CodePattern(PR.Comment, Pattern.compile("^//[^\r\n]*")))
                    fallthroughPatterns.add(CodePattern(PR.Comment, Pattern.compile("^/\\*[\\s\\S]*?(?:\\*\\/|$)")))
                }
                if (regexLiterals != SourceDecoratorOptions.FLAG_DISABLED) {
                    val regexExcl = if (regexLiterals == SourceDecoratorOptions.FLAG_MULTILINE) "" else "\n\r"
                    val regexAny = if (regexExcl.isNotEmpty()) "." else "[\\S\\s]"
                    val regexLiteral = buildString {
                        // A regular expression literal starts with a slash that is
                        // not followed by * or / so that it is not confused with
                        // comments.
                        append("/(?=[^/*$regexExcl])")
                        // and then contains any number of raw characters,
                        append("(?:[^/\\x5B\\x5C$regexExcl]")
                        // escape sequences (\x5C),
                        append("|\\x5C$regexAny")
                        // or non-nesting character sets (\x5B\x5D);
                        append("|\\x5B(?:[^\\x5C\\x5D$regexExcl]")
                        append("|\\x5C$regexAny)*(?:\\x5D|$))+")
                        // finally closed by a /.
                        append("/")
                    }
                    // TODO attach lang
                    fallthroughPatterns.add(
                        CodePattern(
                            PR.Source,
                            Pattern.compile("^$REGEXP_PRECEDER_PATTERN($regexLiteral)")
                        )
                    )
                }
                if (types != null) {
                    fallthroughPatterns.add(CodePattern(PR.Type, types))
                }
                if (keywords.isNotEmpty()) {
                    fallthroughPatterns.add(keywords(*keywords.toTypedArray()))
                }
                fallthroughPatterns.add(
                    CodePattern(
                        PR.Literal,
                        Pattern.compile("^@[a-z_\$][a-z_\$@0-9]*", Pattern.CASE_INSENSITIVE)
                    )
                )
                fallthroughPatterns.add(
                    CodePattern(
                        PR.Type,
                        Pattern.compile("^(?:[@_]?[A-Z]+[a-z][A-Za-z_\$@0-9]*|\\w+_t\\b)")
                    )
                )
                fallthroughPatterns.add(
                    CodePattern(
                        PR.Plain,
                        Pattern.compile("^[a-z_\$][a-z_\$@0-9]*", Pattern.CASE_INSENSITIVE)
                    )
                )
                fallthroughPatterns.add(
                    CodePattern(
                        PR.Literal,
                        Pattern.compile(
                            buildString {

                                append("^(?:")
                                // A hex number
                                append("0x[a-f0-9]+")
                                // or an octal or decimal number,
                                append("|(?:\\d(?:_\\d+)*\\d*(?:\\.\\d*)?|\\.\\d\\+)")
                                // possibly in scientific notation
                                append("(?:e[+\\-]?\\d+)?")
                                append(')')
                                // with an optional modifier like UL for unsigned long
                                append("[a-z]*")
                            }, Pattern.CASE_INSENSITIVE
                        ),
                        "0123456789"
                    )
                )
                // Don't treat escaped quotes in bash as starting strings.
                // See issue 144.
                fallthroughPatterns.add(
                    CodePattern(
                        PR.Plain,
                        Pattern.compile("^\\\\[\\s\\S]?")
                    )
                )
            }
        }
        return fallthroughPatterns
    }

}