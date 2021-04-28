package test.paragraph4.null_checks

fun test() {
    val some: Int? = null
    if (some == null) {
        println("some")
        bar()
    }

    if (some != null) {
        println("some")
        bar()
    }

    if (some == null && true) {
        print("asd")
    }

    if (some == null) {
        print("asd")
    } else {
        print("qwe")
    }

    if (some == null) {
        null
    } else {
        print("qweqwe")
    }

    if (some != null) {
        print("qqq")
    } else {
        print("www")
    }

    if (some != null) {
        print("ttt")
    } else {
        null
    }

    if (some != null) {
        print("ttt")
    } else {
        null
        value
    }
}

