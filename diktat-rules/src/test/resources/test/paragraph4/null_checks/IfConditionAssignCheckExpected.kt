package test.paragraph4.null_checks

fun foo() {
    val x = a?.let {
f(a)
} ?: g(a)

    val y = a ?: 0

    x ?: println("NULL")

    val z = x ?: run {
println("NULL")
0
}

    x?.let {
f(x)
} ?: run {
println("NULL")
g(x)
}
}

fun bar() {
    val x = a?.let {
f(a)
} ?: g(a)

    val y = a ?: 0

    x ?: println("NULL")

    val z = x ?: run {
println("NULL")
0
}

    x?.let {
f(x)
} ?: run {
println("NULL")
g(x)
}
}
