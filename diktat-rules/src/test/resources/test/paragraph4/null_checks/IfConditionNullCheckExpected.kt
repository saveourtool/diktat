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
}
?: run {
print("asd")
}

    some?.let {
print("qqq")
}
?: run {
print("www")
}

    some?.let {
print("ttt")
}
}

