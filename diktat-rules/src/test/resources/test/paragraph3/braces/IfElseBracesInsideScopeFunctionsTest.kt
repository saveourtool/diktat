package test.paragraph3.braces

fun foo1() {
    str.apply {
        if (x > 0) foo()
        else bar()
    }
}

fun foo2() {
    str.let { if (x > 0) { foo() }
    else bar()
    }
}

fun foo3() {
    str.run {
        while (x > 0) {
            if (x > 0) foo()
            else bar()
        }
    }
}

fun foo4() {
    str.with { while (x > 0) {
        if (x > 0) foo()
        else { bar() }
    }
    }
}

fun foo5() {
    str.also {
        while (x > 0) { if (x > 0) foo()
            else bar()
        }
    }
}

fun foo6() {
    str.apply {
        if (x > 0) foo()
        else if (y > 0) abc()
        else bar()
    }
}
