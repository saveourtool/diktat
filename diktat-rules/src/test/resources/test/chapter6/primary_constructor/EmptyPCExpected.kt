package test.chapter6.primary_constructor

class Test {
    var a: Int = 0
    var b: Int = 0
}

class Test {
    var a  = "Property"

    init {
        println("some init")
    }

    constructor(a: String): this() {
        this.a = a
    }
}
