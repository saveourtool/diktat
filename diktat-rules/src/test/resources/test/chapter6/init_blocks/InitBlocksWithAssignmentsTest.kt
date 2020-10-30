package test.chapter6.init_blocks

class A(baseUrl: String) {
    private val customUrl: String
    init {
        customUrl = "$baseUrl/myUrl"
        println("Lorem ipsum")
    }

    init { println("Dolor sit amet") }
}