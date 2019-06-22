package ca.allanwang.gitdroid.codeview.highlighter

var a: Int? = null

fun test() {
    var b = a
    if (a == null) {
        b = 2
        a = b
    }
}