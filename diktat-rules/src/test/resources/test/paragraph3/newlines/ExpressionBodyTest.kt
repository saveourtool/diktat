package test.paragraph3.newlines

fun foo(): String {
    return "lorem ipsum"
}

fun foo():String{
    return "lorem ipsum"
}

fun foo() : String {
    return "lorem ipsum"
}

fun recFoo(): String {
    return "lorem " + recFoo()
}

fun recFoo():String {
    return "lorem " + recFoo()
}

fun recFoo(): String{
    return "lorem " + recFoo()
}

fun foo() = "lorem ipsum"

fun foo() {
    return println("Logging")
}