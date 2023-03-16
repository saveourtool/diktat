package test.paragraph3.file_structure

class Example {
    private val log = LoggerFactory.getLogger(Example.javaClass)

    // lorem ipsum
    private val FOO = 42
    private val BAR = 43

    /**
     * Dolor sit amet
     */
    private val BAZ = 44
    private lateinit var lateFoo: Int
    init {
        bar()
    }

    constructor(baz: Int)

    fun foo() {
        val nested = Nested()
    }

    class Nested {
        val nestedFoo = 43
    }

    companion object {
        private const val ZERO = 0
        private var i = 0
    }

    class UnusedNested { }
}
