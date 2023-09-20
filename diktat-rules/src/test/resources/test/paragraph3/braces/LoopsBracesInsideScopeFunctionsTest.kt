package test.paragraph3.braces

fun foo1() {
    str.apply {
        for (i in 1..100) println(i)
    }
}

fun foo2() {
    str.let {
        for (i in 1..100) println(i)
    }
}

fun foo3() {
    str.run {
        for (i in 1..100) println(i)
    }
}

fun foo4() {
    str.with {
        for (i in 1..100) println(i)
    }
}

fun foo5() {
    str.also {
        for (i in 1..100) println(i)
    }
}
