package test.paragraph3.file_structure

class Example {
    init {
        bar()
    }

    companion object {
        private const val ZERO = 0
        private var i = 0
    }

    private lateinit var lateFoo: Int
    // lorem ipsum
    private val FOO = 42
    private val log = LoggerFactory.getLogger(Example.javaClass)

    class UnusedNested { }

    constructor(baz: Int)

    fun foo() {
        val nested = Nested()
    }

    class Nested {
        val nestedFoo = 43
    }

    private val BAR = 43
    /**
     * Dolor sit amet
     */
    private val BAZ = 44
}
