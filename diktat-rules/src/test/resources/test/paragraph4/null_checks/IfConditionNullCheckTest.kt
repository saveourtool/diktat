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

fun foo() {
    var result: Int? = 10
    while (result != 0 ) {
        if (result != null) {
            goo()
        } else {
            for(i in 1..10)
                break
        }
    }
    while (result != 0) {
        result = goo()
        if (result != null) {
            goo()
        } else {
            println(123)
            break
        }
    }
}

fun checkSmartCases() {
    val x = if (a != null) {
        a.toString()
    } else {
        "Null"
    }
    val y = if (a.b.c != null) {
        a.b.c.toString()
    } else {
        a.b.toString()
    }
    if (a != null) {
        print()
    }
    if (a != null) {
        foo()
    } else {
        boo()
    }
}

fun reversedCheckSmartCases() {
    val x = if (a == null) {
        "Null"
    } else {
        a.toString()
    }
    val y = if (a.b.c == null) {
        a.b.toString()
    } else {
        a.b.c.toString()
    }
    if (a == null) {
        print()
    }
    if (a == null) {
        boo()
    } else {
        foo()
    }
}

fun nullCheckWithAssumption() {
    val a: Int? = 5
    if (a != null) {
        foo()
    } else {
        a = 5
    }
    if (a == null) {
        a = 5
    } else {
        foo()
    }
    if (a != null) {
        a = 5
    } else {
        foo()
    }
    if (a == null) {
        foo()
    } else {
        a = 5
    }
    if (a != null) {
        foo()
    } else {
        a == 5
    }
}
