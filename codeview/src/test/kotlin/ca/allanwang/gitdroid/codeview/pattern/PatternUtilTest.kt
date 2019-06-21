package ca.allanwang.gitdroid.codeview.pattern

import ca.allanwang.gitdroid.codeview.highlighter.CodePattern
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

        fun doNotMatch(vararg input: String, isGlobal: Boolean = false) {
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
        test({ singleQuoted("'").update { it.fullMatch() } }) {
            singleQuoteTest("'")
        }
        test({ singleQuoted("\\\"").update { it.fullMatch() } }) {
            singleQuoteTest("\"")
        }
    }

    @Test
    fun singleAndMultiQuote() {
        testCodePattern({ tripleQuotedStrings().update { it.fullMatch() } }) {
            singleQuoteTest("'")
            singleQuoteTest("\"")
        }
    }

}