package test.paragraph3.sort_error

enum class Alp {
    RED(0xFF0000),
    GREEN(0x00FF00),
    BLUE(0x0000FF),
    ;
}

enum class Warnings {
    WAITING {
        override fun signal() = TALKING
    },
    TALKING {
        override fun signal() = TALKING
    },
    ;

    abstract fun signal(): ProtocolState
}
