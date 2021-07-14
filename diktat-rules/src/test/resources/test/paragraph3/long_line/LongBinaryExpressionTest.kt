package test.paragraph3.long_line

fun foo() {
    val veryLongExpression = Methoooooooooooood() + 12345

    // limit at the left side
    val variable = someField?.let { it.elementType == KDOC } ?: null

    // limit at the right side
    val variable = someField?.let { b == c } ?: null

    // limit at the operation reference
    val variable = someField?.let { bar == fo } ?: null

    var headerKdoc = firstCodeNode.prevSibling { it.elementType == KDOC } ?: if (firstCodeNode == packageDirectiveNode) importsList?.prevSibling { it.elementType == KDOC } else null
}
