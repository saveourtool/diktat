package test.paragraph3.enum_separated

enum class ENUM {
    A , B, C
}

enum class ENUM {
    RED(0xFF0000),
    GREEN(0x00FF00),
    BLUE(0x0000FF) /*sdcsc*/
}

enum class ENUM {
    RED(0xFF0000),
    GREEN(0x00FF00),
    BLUE(0x0000FF),;
}

enum class ProtocolState {
    WAITING {
        override fun signal() = TALKING
    },

    TALKING {
        override fun signal() = WAITING
    };
    abstract fun signal(): ProtocolState
}

enum class ProtocolState {
    WAITING {
        val a = 10;
    },

    TALKING {
        val b = 123;
    };
    abstract fun signal(): ProtocolState
}

enum class Simple {
    A, B, C
}

enum class SimpleWithFun {
    A, B, C;
    fun foo() {}
}

enum class SimpleWithNewLine {
    A, B,
    C
}
