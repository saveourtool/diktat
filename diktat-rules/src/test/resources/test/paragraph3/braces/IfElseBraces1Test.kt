package test.paragraph3.braces

fun foo1() {
    if (x > 0) foo()
    else bar()
}

fun foo2() {
    if (x > 0) foo()
    else {
        println("else")
        bar()
    }
}

fun foo3() {
    if (x > 0)
    else {
        bar()
    }
}

fun foo4() {
    if (x > 0)
    else ;
}

fun foo() {
    if (a) {
        bar()
    } else b?.let {
        baz()
    }
        ?: run {
            qux()
        }
}
