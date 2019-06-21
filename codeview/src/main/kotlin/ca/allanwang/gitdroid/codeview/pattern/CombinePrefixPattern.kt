// Copyright (C) 2006 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package ca.allanwang.gitdroid.codeview.pattern


import java.util.*
import java.util.regex.Pattern


fun List<Pattern>.combine(): Pattern = CombinePrefixPattern().combinePrefixPattern(this)

/**
 * This is similar to the combinePrefixPattern.js in JavaScript Prettify.
 *
 * All comments are adapted from the JavaScript Prettify.
 *
 * @author mikesamuel@gmail.com
 */
class CombinePrefixPattern {

    protected var capturedGroupIndex = 0
    protected var needToFoldCase = false

    /**
     * Given a group of [java.util.regex.Pattern]s, returns a `RegExp` that globally
     * matches the union of the sets of strings matched by the input RegExp.
     * Since it matches globally, if the input strings have a start-of-input
     * anchor (/^.../), it is ignored for the purposes of unioning.
     * @param regexes non multiline, non-global regexes.
     * @return Pattern a global regex.
     */
    @Throws(Exception::class)
    fun combinePrefixPattern(regexes: List<Pattern>): Pattern {
        capturedGroupIndex = 0
        needToFoldCase = false

        var ignoreCase = false

        val azPattern = "[a-z]".toPattern(Pattern.CASE_INSENSITIVE)
        val foldTestRegex = ("\\\\[Uu][0-9A-Fa-f]{4}|\\\\[Xx][0-9A-Fa-f]{2}|\\\\[^UuXx]").toRegex()

        for (r in regexes) {
            if ((r.flags() and Pattern.CASE_INSENSITIVE) != 0) {
                ignoreCase = true
            } else if (azPattern.test(r.pattern().replace(foldTestRegex, ""))) {
                needToFoldCase = true
                ignoreCase = false
                break
            }
        }

        val rewritten: MutableList<String> = mutableListOf()

        for (r in regexes) {
            if ((r.flags() and Pattern.MULTILINE) != 0) {
                throw Exception("Cannot have multiline flag with fold case: ${r.pattern()}")
            }
            rewritten.add("(?:${allowAnywhereFoldCaseAndRenumberGroups(r)})")
        }

        return rewritten.joinToString("|").toPattern(if (ignoreCase) Pattern.CASE_INSENSITIVE else 0)
    }

    private val foldCaseAndRenumberPattern = Pattern.compile(
        "(?:"
                + "\\[(?:[^\\x5C\\x5D]|\\\\[\\s\\S])*\\]" // a character set
                + "|\\\\u[A-Fa-f0-9]{4}" // a unicode escape
                + "|\\\\x[A-Fa-f0-9]{2}" // a hex escape
                + "|\\\\[0-9]+" // a back-reference or octal escape
                + "|\\\\[^ux0-9]" // other escape sequence
                + "|\\(\\?[:!=]" // start of a non-capturing group
                + "|[\\(\\)\\^]" // start/end of a group, or line start
                + "|[^\\x5B\\x5C\\(\\)\\^]+" // run of other characters
                + ")"
    )

    protected fun allowAnywhereFoldCaseAndRenumberGroups(regex: Pattern): String {
        // Split into character sets, escape sequences, punctuation strings
        // like ('(', '(?:', ')', '^'), and runs of characters that do not
        // include any of the above.
        val parts = foldCaseAndRenumberPattern.match(regex.pattern(), true)
        val n = parts.size

        // Maps captured group numbers to the number they will occupy in
        // the output or to -1 if that has not been determined, or to
        // undefined if they need not be capturing in the output.
        val capturedGroups: MutableMap<Int, Int> = mutableMapOf()

        // Walk over and identify back references to build the capturedGroups
        // mapping.
        var groupIndex = 0
        parts.forEachIndexed { i, p ->
            if (p == "(") {
                // groups are 1-indexed, so max group index is count of '('
                ++groupIndex
            } else if ('\\' == p.first()) {
                try {
                    val decimalValue = Math.abs(Integer.parseInt(p.substring(1)))
                    if (decimalValue <= groupIndex) {
                        capturedGroups[decimalValue] = -1
                    } else {
                        // Replace with an unambiguous escape sequence so that
                        // an octal escape sequence does not turn into a backreference
                        // to a capturing group from an earlier regex.
                        parts[i] = encodeEscape(
                            decimalValue
                        )
                    }
                } catch (ex: NumberFormatException) {
                }
            }
        }

        // Renumber groups and reduce capturing groups to non-capturing groups
        // where possible.
        for (i in capturedGroups.keys) {
            if (-1 == capturedGroups[i]) {
                capturedGroups[i] = ++capturedGroupIndex
            }
        }

        parts.forEachIndexed { i, p ->
            if (p == "(") {
                ++groupIndex
                if (capturedGroups[groupIndex] == null) {
                    parts[i] = "(?:"
                }
            } else if ('\\' == p.first()) {
                try {
                    val decimalValue = Math.abs(Integer.parseInt(p.substring(1)))
                    if (decimalValue <= groupIndex) {
                        parts[i] = "\\${capturedGroups[decimalValue]}"
                    }
                } catch (ex: NumberFormatException) {
                }
            }
        }

        // Remove any prefix anchors so that the output will match anywhere.
        // ^^ really does mean an anchored match though.
        for (i in 0 until n) {
            if ("^" == parts[i] && "^" != parts[i + 1]) {
                parts[i] = ""
            }
        }

        // Expand letters to groups to handle mixing of case-sensitive and
        // case-insensitive patterns if necessary.
        if ((regex.flags() and Pattern.CASE_INSENSITIVE) != 0 && needToFoldCase) {
            val azPattern = Pattern.compile("[a-zA-Z]")
            parts.forEachIndexed { i, p ->
                val ch0 = if (p.isNotEmpty()) p[0] else 0
                if (p.length >= 2 && ch0 == '[') {
                    parts[i] = caseFoldCharset(p)
                } else if (ch0 != '\\') {
                    // TODO: handle letters in numeric escapes.
                    val sb = StringBuffer()
                    val _matcher = azPattern.matcher(p)
                    while (_matcher.find()) {
                        val cc = _matcher.group(0).codePointAt(0)
                        _matcher.appendReplacement(sb, "")
                        sb.append("[").append(Character.toString((cc and 32.inv()).toChar()))
                            .append(Character.toString((cc or 32).toChar())).append("]")
                    }
                    _matcher.appendTail(sb)
                    parts[i] = sb.toString()
                }
            }
        }

        return parts.joinToString("")
    }

    companion object {

        protected fun escapeCharToCodeUnit(char: Char): Int? = when (char) {
            'b' -> 8
            't' -> 9
            'n' -> 0xa
            'v' -> 0xb
            'f' -> 0xc
            'r' -> 0xf
            else -> null
        }

        protected fun decodeEscape(charsetPart: String): Int {
            val cc0: Int = charsetPart.codePointAt(0)
            if (cc0 != 92 /* \\ */) {
                return cc0
            }
            val c1 = charsetPart[1]
            val ecc0 = escapeCharToCodeUnit(c1)
            return when {
                ecc0 != null -> ecc0
                c1 in '0'..'7' -> Integer.parseInt(charsetPart.substring(1), 8)
                c1 == 'u' || c1 == 'x' -> Integer.parseInt(charsetPart.substring(2), 16)
                else -> charsetPart.codePointAt(1)
            }
        }

        protected fun encodeEscape(charCode: Int): String {
            if (charCode < 0x20) {
                return (if (charCode < 0x10) "\\x0" else "\\x") + Integer.toString(charCode, 16)
            }

            val ch = String(Character.toChars(charCode))
            return if ((charCode == '\\'.toInt() || charCode == '-'.toInt() || charCode == ']'.toInt() || charCode == '^'.toInt()))
                "\\$ch"
            else
                ch
        }

        fun Pattern.test(input: String) = matcher(input).find()

        private val caseFoldPattern =
            Pattern.compile("\\\\u[0-9A-Fa-f]{4}|\\\\x[0-9A-Fa-f]{2}|\\\\[0-3][0-7]{0,2}|\\\\[0-7]{1,2}|\\\\[\\s\\S]|-|[^-\\\\]")

        private val bdswPattern = Pattern.compile("\\\\[bdsw]", Pattern.CASE_INSENSITIVE)

        protected fun caseFoldCharset(charSet: String): String {
            val charsetParts = caseFoldPattern.match(charSet.substring(1, charSet.length - 1), true)
            val ranges: MutableList<Pair<Int, Int>> = mutableListOf()
            val inverse = charsetParts.getOrNull(0) == "^"

            val out = StringBuilder()
            out.append('[')
            if (inverse) {
                out.append('^')
            }

            var i = if (inverse) 1 else 0
            val n = charsetParts.size
            while (i < n) {
                val p = charsetParts[i]
                if (bdswPattern.test(p)) {  // Don't muck with named groups.
                    out.append(p)
                } else {
                    val start = decodeEscape(p)
                    val end: Int
                    if (i + 2 < n && "-" == charsetParts[i + 1]) {
                        end = decodeEscape(
                            charsetParts[i + 2]
                        )
                        i += 2
                    } else {
                        end = start
                    }
                    ranges.add(start to end)
                    // If the range might intersect letters, then expand it.
                    // This case handling is too simplistic.
                    // It does not deal with non-latin case folding.
                    // It works for latin source code identifiers though.
                    if (!(end < 65 || start > 122)) {
                        if (!(end < 65 || start > 90)) {
                            ranges.add((Math.max(65, start) or 32) to (Math.min(end, 90) or 32))
                        }
                        if (!(end < 97 || start > 122)) {
                            ranges.add((Math.max(97, start) and 32.inv()) to (Math.min(end, 122) and 32.inv()))
                        }
                    }
                }
                i++
            }

            // [[1, 10], [3, 4], [8, 12], [14, 14], [16, 16], [17, 17]]
            // -> [[1, 12], [14, 14], [16, 17]]
            ranges.sortWith(Comparator { (a0, a1), (b0, b1) -> if (a0 != b0) a0 - b0 else b1 - a1 })
            val consolidatedRanges: MutableList<Pair<Int, Int>> = mutableListOf()
            var lastRange = 0 to 0
            for (r in ranges) {
                if (r.first <= lastRange.second + 1) {
                    lastRange = lastRange.first to Math.max(lastRange.second, r.second)
                } else {
                    // reference of lastRange is added
                    lastRange = r
                    consolidatedRanges.add(r)
                }
            }
            for (r in consolidatedRanges) {
                out.append(encodeEscape(r.first))
                if (r.second > r.first) {
                    if (r.second + 1 > r.first) {
                        out.append('-')
                    }
                    out.append(encodeEscape(r.second))
                }
            }
            out.append(']')
            return out.toString()
        }
    }
}