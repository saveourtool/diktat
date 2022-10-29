package test.paragraph4.null_checks

fun test() {
    val some: Int? = null
    some ?: run {
println("some")
bar()
}

    some?.let {
println("some")
bar()
}

    if (some == null && true) {
        print("asd")
    }

    some?.let {
print("qwe")
} ?: print("asd")

    some?.let {
print("qweqwe")
}

    some?.let {
print("qqq")
} ?: print("www")

    some?.let {
print("ttt")
}

    some?.let {
print("ttt")
} ?: run {
null
value
}
}

fun foo() {
    var result: Int? = 10
    while (result != 0 ) {
        result?.let {
goo()
} ?: for(i in 1..10)
break
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
    val x = a?.toString() ?: "Null"
    val y = a.b.c?.toString() ?: a.b.toString()
    a?.let {
print()
}
    a?.let {
foo()
} ?: boo()
}

fun reversedCheckSmartCases() {
    val x = a?.toString() ?: "Null"
    val y = a.b.c?.toString() ?: a.b.toString()
    a ?: print()
    a?.let {
foo()
} ?: boo()
}

fun nullCheckWithAssumption() {
    val a: Int? = 5
    a?.let {
foo()
} ?: run {
a = 5
}
    a?.let {
foo()
} ?: run {
a = 5
}
    a?.let {
a = 5
} ?: foo()
    a?.let {
a = 5
} ?: foo()
    a?.let {
foo()
} ?: a == 5
}
