package test.paragraph3.long_line

fun foobar() {
    foo(
        // ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff
bar()
    )
}

fun foobar() {
    foo(
        //ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff
bar()
    )
}

fun foobar() {
    foo(
        // ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff
bar(),
        // uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu
baz()
    )
}

fun foobar() {
    // ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff
bar()
    // uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu
baz()
}

enum class SomeEnum {
    // ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff
FOO,
    // ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff
BAR,
    // ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff
BAZ,
};

fun foo() {
    when (node.elementType) {
        ElementType.CLASS, ElementType.FUN, ElementType.PROPERTY -> checkBlankLineAfterKdoc(node)
        // ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff
ElementType.IF -> handleIfElse(node)
        ElementType.EOL_COMMENT, ElementType.BLOCK_COMMENT -> handleEolAndBlockComments(node, configuration)
        ElementType.KDOC ->
 handleKdocComments(node, configuration)
        // ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff
else -> {
            // this is a generated else block
        }
    }
}

// aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
fun foo() {
    // bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb
if (a) {
        // cccccccccccccccccccccccccccccccccccccccccc
a()
    // dddddddddddddddddddddddddddddddddddddddddd
} else {
        // eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee
b()
    // ffffffffffffffffffffffffffffffffffffffffff
}
// gggggggggggggggggggggggggggggggggggggggggggggggggggg
}