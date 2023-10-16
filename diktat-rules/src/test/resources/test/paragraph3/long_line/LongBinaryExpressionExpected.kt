package test.paragraph3.long_line

fun foo() {
    val veryLongExpression =
 Methoooooooooooooooood() + 12345

    val veryLongExpression =
 Methoooooooooooooooood() ?: null

    val veryLongExpression = a.Methooooood() +
 b.field

    val variable = someField.filter {
 it.elementType == KDOC
 }

    // limit at the left side
    val variable = a?.filter {
 it.elementType == KDOC
 } ?: null

    // limit at the right side
    val variable = bar?.filter { it.b == c }
 ?: null

    // limit at the operation reference
    val variable = field?.filter { bar == foo }
 ?: null

    val variable = field?.filter { bar == foo }
?: null

    val variable = Methooood() * 2 + 12 + field
 ?: 123 + Methood().linelength

    val variable = Methooood() * 2 + 12 + field
?: 123 + Methood().linelength

    val variable =
 Methoooooooooooooooooooooooooood()
 ?: "some loooooong string"

    val variable = Methooooood()
 ?: "some looong string"

    var headerKdoc = firstCodeNode.prevSibling {
 it.elementType == KDOC
 }
 ?: if (firstCodeNode == packageDirectiveNode) importsList?.prevSibling { it.elementType == KDOC } else null
}
