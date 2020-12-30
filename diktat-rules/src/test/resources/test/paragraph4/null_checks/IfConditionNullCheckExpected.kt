package test.paragraph4.null_checks

fun test() {
    val some: Int? = null
    some ?: kotlin.run {
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
} ?:
kotlin.run {
print("asd")
}

    some?.let {
print("qqq")
} ?:
kotlin.run {
print("www")
}
}

