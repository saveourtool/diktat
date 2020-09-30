package test.paragraph4.generics

class SomeClass(val some: Map<Int, String> = emptyMap()) {
    val myVariable: Map<Int, String> = emptyMap()

    fun someFunc(myVariable: Map<Int, String> = emptyMap()) {}
}
