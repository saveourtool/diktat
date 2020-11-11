package test.chapter6.properties

class Some {
    var prop: Int = 0
        get() = field
        set(value) { field = value }

    val prop2: Int = 0
        get() { return field }

    var propNotChange: Int = 7
        get() { return someCoolLogic(field) }
        set(value) { anotherCoolLogic(value) }

    var testName: String? = null
        private set

    val x = 0
        get
}