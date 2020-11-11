package test.chapter6.properties

class Some {
    var prop: Int = 0

    val prop2: Int = 0

    var propNotChange: Int = 7
        get() { return someCoolLogic(field) }
        set(value) { anotherCoolLogic(value) }

    var testName: String? = null
        private set

    val x = 0
}