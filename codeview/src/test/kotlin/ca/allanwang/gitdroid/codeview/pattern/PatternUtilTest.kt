package ca.allanwang.gitdroid.codeview.pattern

import java.util.regex.Pattern
import kotlin.test.Test
import kotlin.test.assertTrue

class PatternUtilTest {

    fun test(pattern: PatternUtil.() -> Pattern, action: TestContext.() -> Unit) {
        val p = PatternUtil.pattern()
        object : TestContext {
            override fun match(vararg input: String, isGlobal: Boolean) {
                input.forEach {
                    val result = p.match(it, isGlobal)
                    assertTrue(result.isNotEmpty(), "$p: Failed match on $it")
                }
            }

            override fun doNotMatch(vararg input: String, isGlobal: Boolean) {
                input.forEach {
                    val result = p.match(it, isGlobal)
                    assertTrue(result.isEmpty(), "$p: Unexpected match on $it: ${result.contentDeepToString()}")
                }
            }
        }.apply(action)
    }

    interface TestContext {
        fun match(vararg input: String, isGlobal: Boolean = false)
        fun doNotMatch(vararg input: String, isGlobal: Boolean = false)
    }

    @Test
    fun singleQuote() {
        test({ singleQuoted("'").fullMatch() }) {
            match("'hello'", "'escaped\\'escaped'")
            doNotMatch("hello", "asdf'asdf", "'as'as'")
        }
    }

}