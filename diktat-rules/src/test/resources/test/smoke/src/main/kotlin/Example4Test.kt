package test.smoke.src.main.kotlin

fun foo() {
    foo(
            0,
            { obj -> obj.bar() }
    )
}

