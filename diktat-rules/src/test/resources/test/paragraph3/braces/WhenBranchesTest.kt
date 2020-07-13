package test.paragraph3.braces

fun foo(x: Options) {
    when (x) {
        OPTION_1 -> { println(1) }
        OPTION_2 -> {
            println(2)
        }
        OPTION_3 -> println(3)
        else -> {
            println("error")
            throw IllegalStateException()
        }
    }
}