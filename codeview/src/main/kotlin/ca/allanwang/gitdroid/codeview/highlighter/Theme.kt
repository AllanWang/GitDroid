package ca.allanwang.gitdroid.codeview.highlighter

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StyleRes
import ca.allanwang.gitdroid.codeview.BuildConfig
import ca.allanwang.gitdroid.codeview.R
import ca.allanwang.gitdroid.logger.L
import ca.allanwang.kau.utils.withAlpha

interface CodeTheme {
    @ColorInt
    fun prColor(pr: PR): Int

    val contentBg: Int
        @ColorInt get
    val lineNumTextColor: Int
        @ColorInt get
    val lineNumBg: Int
        @ColorInt get

    companion object {
        fun default(): CodeTheme = object : CodeTheme {
            override fun prColor(pr: PR): Int = when (pr) {
                PR.AttrName -> 0x268BD2
                PR.AttrValue -> 0x269186
                PR.Comment -> 0x93A1A1
                PR.Declaration -> 0x268BD2
                PR.Keyword -> 0x268BD2
                PR.Literal -> 0x269186
                PR.Nocode -> 0x000000
                PR.Plain -> 0x586E75
                PR.Punctuation -> 0x586E75
                PR.Source -> 0xff00ff
                PR.String -> 0x269186
                PR.Tag -> 0x859900
                PR.Type -> 0x859900
            }

            override val contentBg: Int = 0xE9EDF4
            override val lineNumTextColor: Int = 0x99A8B7
            override val lineNumBg: Int = 0xF2F2F6
        }.withOpaqueColors()
    }
}

open class CodeAttrThemeBuilder {

    open val contentBgAttr: Int = R.attr.codeContentBackgroundColor
        @ColorRes get

    open val lineNumTextColorAttr: Int = R.attr.codeLineNumTextColor
        @ColorRes get

    open val lineNumBgAttr: Int = R.attr.codeLineNumBackgroundColor
        @ColorRes get

    @ColorRes
    open fun prColorAttr(pr: PR): Int = when (pr) {
        PR.AttrName -> R.attr.prAttrNameColor
        PR.AttrValue -> R.attr.prAttrValueColor
        PR.Comment -> R.attr.prCommentColor
        PR.Declaration -> R.attr.prDeclarationColor
        PR.Keyword -> R.attr.prKeywordColor
        PR.Literal -> R.attr.prLiteralColor
        PR.Nocode -> R.attr.prNocodeColor
        PR.Plain -> R.attr.prPlainColor
        PR.Punctuation -> R.attr.prPunctuationColor
        PR.Source -> R.attr.prSourceColor
        PR.String -> R.attr.prStringColor
        PR.Tag -> R.attr.prTagColor
        PR.Type -> R.attr.prTypeColor
    }

    fun build(
        context: Context,
        attrs: AttributeSet? = null,
        @AttrRes defStyleAttr: Int = R.attr.codeViewStyle,
        @StyleRes defStyleRes: Int = 0,
        fallback: (() -> CodeTheme)? = null
    ): CodeTheme {
        val attributes: List<Int> = (listOf(
            contentBgAttr,
            lineNumTextColorAttr,
            lineNumBgAttr
        ) + PR.values().map { prColorAttr(it) }).distinct()

        context.theme.obtainStyledAttributes(attrs, attributes.toIntArray(), defStyleAttr, defStyleRes).apply {
            try {
                val fallbackTheme: CodeTheme? by lazy { fallback?.invoke() }
                fun color(tag: String, index: Int, fallbackColor: CodeTheme.() -> Int): Int {
                    if (hasValue(index)) {
                        return getColor(index, 0)
                    }
                    if (BuildConfig.DEBUG) {
                        L._d { "Theme attr not defined for $tag" }
                    }
                    return fallbackTheme?.fallbackColor()
                        ?: throw RuntimeException("Theme builder does not have attribute nor fallback for $tag")
                }

                val contentBg = color("contentBg", 0) { contentBg }
                val lineNumTextColor = color("lineNumTextColor", 1) { lineNumTextColor }
                val lineNumBg = color("lineNumBg", 2) { lineNumBg }

                val plainColor = color("PR.${PR.Plain}", PR.Plain.ordinal + 3) { prColor(PR.Plain) }

                val colorMap =
                    PR.values().map { it to color("PR.${it.name}", it.ordinal + 3) { plainColor } }.toMap()
                return object : CodeTheme {
                    override fun prColor(pr: PR): Int = colorMap.getValue(pr)

                    override val contentBg: Int = contentBg
                    override val lineNumTextColor: Int = lineNumTextColor
                    override val lineNumBg: Int = lineNumBg
                }
            } finally {
                recycle()
            }
        }
    }

    companion object {
        fun default(): CodeAttrThemeBuilder = CodeAttrThemeBuilder()
    }

}

/**
 * Sets all the color opacities to 0xFF
 */
class OpaqueTheme(theme: CodeTheme) : CodeTheme {

    private val _prColors = PR.values().map { theme.prColor(it).withAlpha(255) }.toIntArray()
    private val _contentBg = theme.contentBg.withAlpha(255)
    private val _lineNumTextColor = theme.lineNumTextColor.withAlpha(255)
    private val _lineNumBg = theme.lineNumBg.withAlpha(255)

    override fun prColor(pr: PR): Int = _prColors[pr.ordinal]
    override val contentBg: Int = _contentBg
    override val lineNumTextColor: Int = _lineNumTextColor
    override val lineNumBg: Int = _lineNumBg
}

fun CodeTheme.withOpaqueColors(): CodeTheme = if (this is OpaqueTheme) this else OpaqueTheme(this)