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

fun foo5() {
    if (x > 0)
        foo()
    else
        bar()
}

fun foo6() {
    if (x > 0) foo()
    else if (y > 0) abc()
    else bar()
}

fun foo7() {
    if (x > 0)
        foo()
    else if (y > 0)
        abc()
    else
        bar()
}

fun foo8() {
    if (x > 0) if (y > 0) foo() else abc()
    else bar()
}

fun foo9() {
    if (x > 0) foo()
    else if (y > 0) abc() else bar()
}

fun foo10() {
    if (x > 0) foo()
    else if (z > 0) if (y > 0) abc() else qwe()
    else bar()
}

fun foo11() {
    if (x > 0) else bar()
}

fun foo12() {
    if (x > 0) foo() else ;
}

fun foo13() {
    if (x > 0) else ;
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

fun foo() {
    if (a) {
        bar()
    } else b.apply {
        baz()
    }
}

fun foo() {
    if (a) {
        bar()
    } else baz(b.apply { id = 5 })
}
