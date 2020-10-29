package test.chapter6.init_blocks

class A(baseUrl: String) {
    private val customUrl: String = "$baseUrl/myUrl"
    init {
        println("Lorem ipsum")
println("Dolor sit amet")
    }

    }