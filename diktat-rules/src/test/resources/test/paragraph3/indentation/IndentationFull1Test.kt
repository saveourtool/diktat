package test.paragraph3.indentation

data class Example(val field1: Type1,
val field2: Type2) {
/**
 * Lorem ipsum
 * dolor sit amet
 */
fun foo(
    a: Int,
    b: Int
): Int {
            return a + b
    }

    fun bar() {
        for (i in 1..100)
        println(i)

        do
        println()
        while (condition)

        for (i in 1..100) {
        println(i)
        }
    }

    fun baz() {
        if (condition)
        foobar()
        else
        foobaz()
    }

    fun some() {
        val a = "${
        foo().bar()
        }"

        val b = "${baz().foo()}"
    }
}
