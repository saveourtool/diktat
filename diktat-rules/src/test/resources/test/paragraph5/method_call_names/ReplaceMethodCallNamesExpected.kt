package test.chapter6.method_call_names

fun coolFunction() {
    val list = listOf(1, 2, 3)
    val testStr = ""

    if (list.isEmpty()) { }

    if (list.isNotEmpty()) { }

    if (list.isNotEmpty()) { }

    if (list.isEmpty()) { }

    if (testStr.isBlank()) { }

    if (testStr.isNotBlank()) { }

    if (testStr.isNotBlank()) { }

    if (testStr.isBlank()) { }

    if (list.isNotEmpty() && testStr.isBlank()) { }
}

