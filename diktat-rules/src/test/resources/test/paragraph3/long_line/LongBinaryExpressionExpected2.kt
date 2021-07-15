package test.paragraph3.long_line

fun foo() {
    var headerKdoc = firstCodeNode.prevSibling { it.elementType == KDOC } ?:
 if (firstCodeNode == packageDirectiveNode) importsList?.prevSibling { it.elementType == KDOC } else null
}