package test.paragraph4.generics

class SomeClass(val some: Map<Int, String> = emptyMap<Int, String>()) {
    val myVariable: Map<Int, String> = emptyMap<Int, String>()

    fun someFunc(myVariable: Map<Int, String> = emptyMap<Int, String>()) {}
}
