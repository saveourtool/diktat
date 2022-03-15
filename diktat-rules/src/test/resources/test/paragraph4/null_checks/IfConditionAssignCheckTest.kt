package test.paragraph4.null_checks

fun foo() {
    val x = if (a != null) f(a) else g(a)

    val y = if (a != null) a else 0

    if (x != null) {
        x
    } else {
        println("NULL")
    }

    val z = if (x != null) {
        x
    } else {
        println("NULL")
        0
    }

    if (x != null) {
        f(x)
    } else {
        println("NULL")
        g(x)
    }
}

fun bar() {
    val x = if (a == null) g(a) else f(a)

    val y = if (a == null) 0 else a

    if (x == null) {
        println("NULL")
    } else {
        x
    }

    val z = if (x == null) {
        println("NULL")
        0
    } else {
        x
    }

    if (x == null) {
        println("NULL")
        g(x)
    } else {
        f(x)
    }
}
