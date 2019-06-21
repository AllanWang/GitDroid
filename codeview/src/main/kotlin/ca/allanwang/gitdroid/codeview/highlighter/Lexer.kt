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
package ca.allanwang.gitdroid.codeview.highlighter

import java.util.regex.Pattern

/**
 * Based around:
 * - https://github.com/google/code-prettify/blob/master/src/prettify.js
 * - https://gerrit.googlesource.com/java-prettify/+/master/src/prettify/parser/Prettify.java
 *
 * While the logic is primarily the same, this is a complete rewrite to match some changes in other interfaces.
 * Some functions are further optimized towards Kotlin.
 */
internal class Lexer(shortcutPatterns: List<CodePattern>, private val fallbackPatterns: List<CodePattern>) {

    private val shortcuts: Map<Char, CodePattern>
    private val tokenizer: Pattern


    init {
        val shortcuts: MutableMap<Char, CodePattern> = mutableMapOf()
        val regexKeys: MutableSet<String> = mutableSetOf()
        val allRegexes: MutableList<Pattern> = mutableListOf()
        (shortcutPatterns.asSequence() + fallbackPatterns.asSequence()).forEach { p ->
            if (p.shortcut == null) {
                return@forEach
            }
            (p.shortcut.lastIndex downTo 0).forEach {
                shortcuts[p.shortcut[it]] = p
            }
            val k = p.pattern.pattern()
            if (k !in regexKeys) {
                allRegexes.add(p.pattern)
            }
        }
        allRegexes.add("[\u0000-\\uffff]".toPattern())
        this.tokenizer = CombinePrefixPattern().combinePrefixPattern(allRegexes)
        this.shortcuts = shortcuts

    }

    fun decorate(job: Job): Job {
        val decorations: MutableList<Decoration> = mutableListOf(Decoration(job.basePos, CodeHighlighter.PR.Plain))
        var pos = 0
        var embedded = false
        val tokens = tokenizer.match(job.source, true)
        val styleCache: MutableMap<String, CodeHighlighter.PR> = mutableMapOf()
        var match: Array<String>? = null

        fun addDecor(pos: Int, style: CodeHighlighter.PR?) {
            decorations.add(
                Decoration(
                    job.basePos + pos,
                    style ?: CodeHighlighter.PR.Plain
                )
            )
        }

        for (token in tokens) {
            var style: CodeHighlighter.PR? = styleCache[token]
            if (style == null) {
                val shortcutPattern = shortcuts[token[0]]
                if (shortcutPattern != null) {
                    match = shortcutPattern.pattern.match(token, false)
                    style = shortcutPattern.pr
                } else {
                    for (fallthroughPattern in fallbackPatterns) {
                        match = fallthroughPattern.pattern.match(token, false)
                        if (match.isNotEmpty()) {
                            style = fallthroughPattern.pr
                            break
                        }
                    }
                }
                // Unlike google's variant, we don't have custom keys
                // All lang- patterns should be labelled source
                embedded = style == CodeHighlighter.PR.Source
                if (embedded && match?.get(1) != null) {
                    embedded = false
                }
                if (!embedded) {
                    styleCache[token] = style ?: CodeHighlighter.PR.Plain
                }
            }

            val tokenStart = pos
            pos += token.length

            if (!embedded) {
                addDecor(tokenStart, style)
            } else {
                val embeddedSource = match!!.get(1)
                var embeddedSourceStart = token.indexOf(embeddedSource)
                var embeddedSourceEnd = embeddedSourceStart + embeddedSource.length
                if (match.getOrNull(2) != null) {
                    // If embeddedSource can be blank, then it would match at the
                    // beginning which would cause us to infinitely recurse on the
                    // entire token, so we catch the right context in match[2].
                    embeddedSourceEnd = token.length - match[2].length
                    embeddedSourceStart = embeddedSourceEnd - embeddedSource.length
                }
                val lang = ""// style.substring(5)
                val subJobs = listOf(
                    // Decorate the left of the embedded source
                    appendDecorations(
                        job.basePos + tokenStart,
                        token.substring(0, embeddedSourceStart)
                    ),
                    // Decorate the embedded source
                    appendDecorations(
                        job.basePos + tokenStart + embeddedSourceStart,
                        embeddedSource
                    ),
                    // Decorate the right of the embedded section
                    appendDecorations(
                        job.basePos + tokenStart + embeddedSourceEnd,
                        token.substring(embeddedSourceEnd)
                    )
                ).forEach {
                    decorations.addAll(it.decorations)
                }
            }
        }

        return job.copy(decorations = removeDuplicates(decorations, job.source))
    }

    companion object {
        /**
         * Shortens decoration list to remove unnecessary entries.
         * Namely, if multiple decorations reference the same position, we use the last entry.
         * If multiple sequential decorations have the same style, we only keep the first one.
         * If there is a decoration for the very last index, we ignore it.
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
                    iter.next()
                    prevDecor = d
                } else if (d.pr == prevDecor.pr) {
                    iter.remove()
                }
            }

            // remove last zero length tag
            if (results.size >= 2 && results.last().pos == source.length) {
                results.removeAt(results.lastIndex)
            }
            return results
        }
    }

    internal fun appendDecorations(basePos: Int, sourceCode: String): Job {
        val job = Job(basePos, sourceCode, emptyList())
        return decorate(job)
    }
}