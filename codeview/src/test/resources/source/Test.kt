package source

/**
 * KtDocs
 */
class Test {

    // Single comment
    data class A(val s: Int, val y: Boolean)

    init {
        val lit = 0
        val lit2 = 0xff00ff
        val s = "hello"
        val s2 =
            """
            long
            string
            """.trimIndent()
    }

    /*
     * Multi line comment
     */
    @Volatile
    var hello: Int = 2

    fun a(t: Test) {
        with(t) {
            return@with
        }
    }

    open fun getTask(isTrue: Boolean): String {
        TODO("todo")
    }


}