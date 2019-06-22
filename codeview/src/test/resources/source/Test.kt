package source

class Test {

    data class A(val s: Int, val y: Boolean)

    init {

    }

    @Volatile
    var hello: Int = 2

    fun a(t: Test) {
        with(t) {
            return@with
        }
    }


}