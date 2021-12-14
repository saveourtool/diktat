package test.paragraph3.long_line

fun foobar() {
    foo(
        bar() // ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff
    )
}

fun foobar() {
    foo(
        bar()//ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff
    )
}

fun foobar() {
    foo(
        bar(), // ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff
        baz()  // uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu
    )
}

fun foobar() {
    bar()  // ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff
    baz()  // uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu
}

enum class SomeEnum {
    FOO, // ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff
    BAR, // ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff
    BAZ, // ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff
};

fun foo() {
    when (node.elementType) {
        ElementType.CLASS, ElementType.FUN, ElementType.PROPERTY -> checkBlankLineAfterKdoc(node)
        ElementType.IF -> handleIfElse(node) // ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff
        ElementType.EOL_COMMENT, ElementType.BLOCK_COMMENT -> handleEolAndBlockComments(node, configuration)
        ElementType.KDOC -> handleKdocComments(node, configuration)
        else -> { // ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff
            // this is a generated else block
        }
    }
}

fun foo() { // aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
    if (a) { // bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb
        a() // cccccccccccccccccccccccccccccccccccccccccc
    } else { // dddddddddddddddddddddddddddddddddddddddddd
        b() // eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee
    } // ffffffffffffffffffffffffffffffffffffffffff
} // gggggggggggggggggggggggggggggggggggggggggggggggggggg