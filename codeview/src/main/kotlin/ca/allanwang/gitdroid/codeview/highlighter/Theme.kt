package ca.allanwang.gitdroid.codeview.highlighter

import androidx.annotation.ColorInt
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

fun CodeTheme.withOpaqueColors(): CodeTheme = OpaqueTheme(this)