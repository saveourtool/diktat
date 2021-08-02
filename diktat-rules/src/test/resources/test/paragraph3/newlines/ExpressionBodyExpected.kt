package test.paragraph3.newlines

fun foo(): String = "lorem ipsum"

fun foo():String = "lorem ipsum"

fun foo() : String = "lorem ipsum"

fun recFoo(): String = "lorem " + recFoo()

fun recFoo():String = "lorem " + recFoo()

fun recFoo(): String = "lorem " + recFoo()

fun foo() = "lorem ipsum"

fun foo() = println("Logging")