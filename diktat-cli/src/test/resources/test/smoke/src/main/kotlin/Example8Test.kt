package com.saveourtool.diktat

fun foo() {
    val sum: (Int, Int, Int,) -> Int = fun(
        x,
        y,
        z
    ): Int {
        return x + y + x
    }
    println(sum(8, 8, 8))
}

fun boo() {
    val message = fun()=println("Hello")
}