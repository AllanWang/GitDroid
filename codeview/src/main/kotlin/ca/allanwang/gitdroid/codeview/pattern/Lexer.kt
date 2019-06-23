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

import ca.allanwang.gitdroid.codeview.highlighter.PR
import ca.allanwang.gitdroid.codeview.language.impl.CodeLanguage
import java.util.regex.Pattern

/**
 * Based around:
 * - https://github.com/google/code-prettify/blob/master/src/prettify.js
 * - https://gerrit.googlesource.com/java-prettify/+/master/src/prettify/parser/Prettify.java
 *
 * While the logic is primarily the same, this is a complete rewrite to match some changes in other interfaces.
 * Some functions are further optimized towards Kotlin.
 *
 * Lexers are no longer supplied with lists of patterns.
 * Instead they are supplied with languages, which are essentially pattern factories
 */
class Lexer internal constructor(
    lang: CodeLanguage,
    private val listener: Listener?
) {

    constructor(lang: CodeLanguage) : this(lang, null)

    private val fallthroughPatterns: List<CodePattern>
    private val shortcuts: Map<Char, CodePattern>
    private val tokenizer: Pattern

    init {
        val shortcutPatterns = lang.shortcutPatterns()
        fallthroughPatterns = lang.fallthroughPatterns()
        val shortcuts: MutableMap<Char, CodePattern> = mutableMapOf()
        val regexKeys: MutableSet<String> = mutableSetOf()
        val allRegexes: MutableList<Pattern> = mutableListOf()
        (shortcutPatterns.asSequence() + fallthroughPatterns.asSequence()).forEach { p ->
            val k = p.pattern.pattern()
            if (k !in regexKeys) {
                allRegexes.add(p.pattern)
            }
            if (p.shortcut == null) {
                return@forEach
            }
            (p.shortcut.lastIndex downTo 0).forEach {
                shortcuts[p.shortcut[it]] = p
            }
        }
        allRegexes.add(Pattern.compile("[\\u0000-\\uffff]"))
        this.tokenizer = allRegexes.combine()
        this.shortcuts = shortcuts
        listener?.onInit(this.tokenizer, this.shortcuts)

    }

    internal fun tokens(content: String): Array<String> =
        tokenizer.match(content, true).filterNotNull().toTypedArray()


    internal interface Listener {
        fun onInit(tokenizer: Pattern, shortcuts: Map<Char, CodePattern>)
        fun onNewDecor(token: String, pos: Int, pattern: CodePattern?, match: Array<String?>?)
    }

    suspend fun decorate(content: String): List<Decoration> = decorate(LexerJob(0, content))

    /**
     * Main function that extracts decorations recursively.
     */
    suspend fun decorate(job: LexerJob): List<Decoration> {
        val decorations: MutableList<Decoration> = mutableListOf(
            Decoration(
                job.basePos,
                PR.Plain
            )
        )
        var pos = 0
        val tokens = tokens(job.source)
        val styleCache: MutableMap<String, PR> = mutableMapOf()

        fun addDecor(pos: Int, style: PR) {
            decorations.add(
                Decoration(
                    job.basePos + pos,
                    style
                )
            )
        }

        for (token in tokens) {
            val tokenStart = pos
            pos += token.length
            val cached: PR? = styleCache[token]
            if (cached != null) {
                addDecor(tokenStart, cached)
                continue
            }
            var match: Array<String?>? = null
            var pattern: CodePattern? = null

            val shortcutPattern = shortcuts[token[0]]
            if (shortcutPattern != null) {
                match = shortcutPattern.pattern.match(token, false)
                pattern = shortcutPattern
            } else {
                for (fallthroughPattern in fallthroughPatterns) {
                    match = fallthroughPattern.pattern.match(token, false)
                    if (match.isNotEmpty()) {
                        pattern = fallthroughPattern
                        break
                    }
                }
            }

            listener?.onNewDecor(token, tokenStart, pattern, match)

            val style = pattern?.pr ?: PR.Plain
            // Unlike google's variant, we don't have custom keys
            // All lang- patterns should be labelled source
            val embedded = style == PR.Source && match?.getOrNull(1) != null
            if (!embedded) {
                styleCache[token] = style
                addDecor(tokenStart, style)
            } else {
                // TODO add embedded source
                val embeddedSource = match?.getOrNull(1) ?: continue
                var embeddedSourceStart = token.indexOf(embeddedSource)
                var embeddedSourceEnd = embeddedSourceStart + embeddedSource.length
                val match2 = match.getOrNull(2)
                if (match2 != null) {
                    // If embeddedSource can be blank, then it would match at the
                    // beginning which would cause us to infinitely recurse on the
                    // entire token, so we catch the right context in match[2].
                    embeddedSourceEnd = token.length - match2.length
                    embeddedSourceStart = embeddedSourceEnd - embeddedSource.length
                }
                // TODO coroutines?
                listOf(
                    // Decorate the left of the embedded source
                    decorate(
                        LexerJob(
                            job.basePos + tokenStart,
                            token.substring(0, embeddedSourceStart)
                        )
                    ),
                    // Decorate the embedded source
                    decorate(
                        LexerJob(
                            job.basePos + tokenStart + embeddedSourceStart,
                            embeddedSource
                        )
                    ),
                    // Decorate the right of the embedded section
                    decorate(
                        LexerJob(
                            job.basePos + tokenStart + embeddedSourceEnd,
                            token.substring(embeddedSourceEnd)
                        )
                    )
                ).forEach {
                    decorations.addAll(it)
                }
            }
        }

        return removeDuplicates(decorations, job.source)
    }

    companion object {

        /**
         * Shortens decoration list to remove unnecessary entries.
         * Namely, if multiple decorations reference the same position, we use the last entry.
         * If multiple sequential decorations have the same style, we only keep the first one.
         * We will remove all decorations that exceed the source length, and guarantee that
         * the list ends with a plain color resetter at the last index
         *
         * Google's implementation uses a treemap, but I achieved the same results with just a list sort.
         * We don't need O(1) access by position.
         */
        internal fun removeDuplicates(decorations: List<Decoration>, source: String): List<Decoration> {
            if (decorations.isEmpty()) {
                return emptyList()
            }
            // Sorting is stable, so equal positions are still in the same order
            val results: MutableList<Decoration> = decorations.sortedBy { it.pos }.toMutableList()
            val iter = results.listIterator()
            // We won't ever skip the first one but we can use it as reference
            var prevDecor: Decoration = iter.next()
            while (iter.hasNext()) {
                val d = iter.next()
                if (d.pos == prevDecor.pos) {
                    iter.previous()
                    iter.remove()
                } else if (d.pr == prevDecor.pr) {
                    iter.remove()
                }
                prevDecor = d
            }

            // remove last zero length tag
            while (results.size >= 2 && results.last().pos >= source.length) {
                results.removeAt(results.lastIndex)
            }
            results.add(Decoration(source.length, PR.Plain))
            return results
        }
    }
}