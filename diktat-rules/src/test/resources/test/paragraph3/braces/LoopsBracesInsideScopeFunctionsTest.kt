package test.paragraph3.braces

fun foo1() {
    str.apply {
        for (i in 1..100) println(i)
    }
}

fun foo2() {
    str.let { while (x > 0) println(i)
    }
}

fun foo3() {
    str.run {
        do println(i)
        while (x > 0)
    }
}

fun foo4() {
    str.with { do println(i)
    while (x > 0)
    }
}

fun foo5() {
    str.also {
        for (i in 1..100) {
            while (x > 0)
                println(i)
        }
    }
}
