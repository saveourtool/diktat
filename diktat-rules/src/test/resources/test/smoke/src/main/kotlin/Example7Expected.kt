package org.cqfn.diktat

fun foo() {
    val prop: Int = 0

    prop ?: run {
        println("prop is null")
        bar()
    }

    prop?.let {
        baz()
        gaz()
    }

    prop?.let {
        doAnotherSmth()
    }
        ?: run {
            doSmth()
        }
}

fun fooo() {
    if (a) {
        bar()
    } else b?.let {
        baz()
    }
        ?: run {
            qux()
        }
}
