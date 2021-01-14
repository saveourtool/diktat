package test.chapter6.method_call_names

fun coolFunction() {
    val list = listOf(1, 2, 3)
    val testStr = ""

    if (list.isEmpty()) { }

    if (list.isNotEmpty()) { }

    if (!list.isEmpty()) { }

    if (!list.isNotEmpty()) { }

    if (testStr.isBlank()) { }

    if (testStr.isNotBlank()) { }

    if (!testStr.isBlank()) { }

    if (!testStr.isNotBlank()) { }

    if (!list.isEmpty() && !testStr.isNotBlank()) { }
}

