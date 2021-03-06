package ca.allanwang.gitdroid.codeview.pattern

import java.util.regex.Pattern
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PatternUtilTest {

    fun test(pattern: PatternUtil.() -> Pattern, action: TestContext.() -> Unit) {
        val p = PatternUtil.pattern()
        TestContext.from(p).apply(action)
    }

    fun testCodePattern(pattern: CodePatternUtil.() -> CodePattern, action: TestContext.() -> Unit) {
        val p = CodePatternUtil.pattern().pattern
        TestContext.from(p).apply(action)
    }

    interface TestContext {
        val p: Pattern

        fun match(vararg input: String, isGlobal: Boolean = false) {
            input.forEach {
                assertTrue(p.matcher(it).matches(), "$p - Failed match on $it")
            }
        }

        fun find(input: String, vararg results: String) {
            val m = p.matcher(input)
            assertTrue(m.find(), "$p - could not find $input")
            val actual = (1..m.groupCount()).map { m.group(it) }
            assertEquals(results.toList(), actual, "$p - Find mismatch for $input")
        }

        fun doNotFind(vararg input: String) {
            input.forEach {
                val m = p.matcher(it)
                assertFalse(m.find(), "$p - unexpected find on $it")
            }
        }

        fun doNotMatch(vararg input: String) {
            input.forEach {
                assertFalse(p.matcher(it).matches(), "$p - Unexpected match on $it")
            }
        }

        fun withPattern(p: Pattern, action: TestContext.() -> Unit) {
            from(p).apply(action)
        }

        companion object {
            fun from(pattern: Pattern): TestContext =
                object : TestContext {
                    override val p: Pattern = pattern
                }
        }
    }

    fun TestContext.singleQuoteTest(s: String) {
        match("${s}hello$s", "${s}escaped\\${s}escaped$s")
        doNotMatch("hello", "asdf${s}asdf", "${s}as${s}as$s")
    }

    @Test
    fun stringWrap() {
        val word = "hello"
        with(PatternUtil) {
            assertEquals("^(?:$word)", word.fromStart())
            assertEquals("^(?:$word)", ("(?:$word)").fromStart())
            assertEquals("^(?:$word)$", word.fullMatch())
            assertEquals("^(?:$word)$", ("(?:$word)").fullMatch())
        }
    }

    @Test
    fun singleQuote() {
        test({ singleQuoted("'") }) {
            singleQuoteTest("'")
        }
        test({ singleQuoted("\\\"") }) {
            singleQuoteTest("\"")
        }
    }

    @Test
    fun singleAndMultiQuote() {
        testCodePattern({ tripleQuotedStrings() }) {
            singleQuoteTest("'")
            singleQuoteTest("\"")
        }
    }

    @Test
    fun singleLineQuoted() {
        testCodePattern({ singleLineStrings() }) {
            singleQuoteTest("'")
            singleQuoteTest("\"")
        }
    }

    @Test
    fun keywords() {
        test({ keywords("hello", "world", blockFront = true) }) {
            match("hello", "world")
            doNotMatch("ahello", "helloa", "hllo", "helloworld")
            find("hello a")
            doNotFind("ahello", "helloA", "helloworld")
        }
    }

}