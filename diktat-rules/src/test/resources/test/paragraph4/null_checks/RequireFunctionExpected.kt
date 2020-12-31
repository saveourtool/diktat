package test.paragraph4.null_checks

fun test() {
    val some: Int? = null

    requireNotNull(some)
}

