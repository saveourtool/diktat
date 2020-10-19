package test.paragraph3.spaces

class Example : SuperExample {
    constructor(val a: Int)

    fun foo() {
        if (condition) { }
        for (i in 1..100) { }
        when (expression) { }
    }

    fun bar() {
        if (condition) { }
        for (i in 1..100) { }
        when (expression) { }
    }
}

data class Example(
    val foo: Foo,
    val bar: Bar
)
