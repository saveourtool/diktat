package test.paragraph3.sort_error

enum class Alp {
    BLUE(0x0000FF),
    GREEN(0x00FF00),
    RED(0xFF0000),
;
}

enum class Warnings {
    TALKING {
        override fun signal() = TALKING
    },
    WAITING {
        override fun signal() = TALKING
    },
;

    abstract fun signal(): ProtocolState
}

enum class Warnings {
    TALKING {
        override fun signal() = TALKING
    },
    WAITING {
        override fun signal() = TALKING
    };

    abstract fun signal(): ProtocolState
}

enum class Alp {
    BLUE(0x0000FF),
    GREEN(0x00FF00),
    RED(0xFF0000),
}

enum class Alp {
    BLUE(0x0000FF),
    GREEN(0x00FF00),
    RED(0xFF0000)
;
}

enum class IssueType {
    PROJECT_STRUCTURE, TESTS, VCS
}

enum class IssueType {
    PROJECT_STRUCTURE,TESTS,VCS
}

enum class IssueType {
    PROJECT_STRUCTURE, // comment
TESTS,
VCS
}

enum class IssueType {
    PROJECT_STRUCTURE ,// comment
TESTS,
VCS
}
