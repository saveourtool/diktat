package test.paragraph4.null_checks

fun foo() {
    val x = if (a != null) f(a) else g(a)
    val y = if (a != null) a else 0
    if (x != null) {
        x
    } else {
        println("NULL")
    }
}

fun bar() {
    val x = if (a == null) g(a) else f(a)
    val y = if (a == null) 0 else a
}
