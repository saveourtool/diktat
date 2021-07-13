package test.paragraph3.long_line

fun foo() {
    val veryLongExpression = Method() + 12345 + baaar()

    var headerKdoc = firstCodeNode.prevSibling { it.elementType == KDOC } ?: if (firstCodeNode == packageDirectiveNode) importsList?.prevSibling { it.elementType == KDOC } else null
}
