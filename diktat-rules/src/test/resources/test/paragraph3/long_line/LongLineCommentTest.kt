package test.paragraph3.long_line

// Hello World! This is a first part of comment. This is a very long comment that cannot be split
fun foo() {
    val namesList = listOf<String>("Jack", "Nick")
    namesList.forEach { name ->
        if (name == "Nick") {
            namesList.map {
                it.subSequence(0, 1) // This is another comment inside map
                it.split("this is long regex") // this comment start to the right of max length
            }
        }
    }
}

/**
 * This rule checks if there is a backing property for field with property accessors, in case they don't use field keyword
 */
class ImplicitBackingPropertyRule(configRules: List<RulesConfig>) : DiktatRule(
    "implicit-backing-property",
    configRules,
    listOf(NO_CORRESPONDING_PROPERTY)) {
    override fun logic(node: ASTNode) {
        if (node.elementType == CLASS_BODY) {
            findAllProperties(node)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun findAllProperties(node: ASTNode) {
        val properties = node.getChildren(null).filter { it.elementType == PROPERTY }

        val propsWithBackSymbol = properties
            .filter { it.getFirstChildWithType(IDENTIFIER)!!.text.startsWith("_") }
            .map {
                it.getFirstChildWithType(IDENTIFIER)!!.text
            }

        properties.filter { it.hasAnyChildOfTypes(PROPERTY_ACCESSOR) }.forEach {
            validateAccessors(it, propsWithBackSymbol)
        }
    }

    private fun validateAccessors(node: ASTNode, propsWithBackSymbol: List<String>) {
        val accessors = node.findAllDescendantsWithSpecificType(PROPERTY_ACCESSOR).filter { it.hasChildOfType(BLOCK) }  // exclude get with expression body

        accessors.filter { it.hasChildOfType(GET_KEYWORD) }.forEach { handleGetAccessors(it, node, propsWithBackSymbol) }
        accessors.filter { it.hasChildOfType(SET_KEYWORD) }.forEach { handleSetAccessors(it, node, propsWithBackSymbol) }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleGetAccessors(
        accessor: ASTNode,
        node: ASTNode,
        propsWithBackSymbol: List<String>) {
        val refExprs = accessor
            .findAllDescendantsWithSpecificType(RETURN)
            .filterNot { it.hasChildOfType(DOT_QUALIFIED_EXPRESSION) }
            .flatMap { it.findAllDescendantsWithSpecificType(REFERENCE_EXPRESSION) }

        val localProps = accessor
            .findAllDescendantsWithSpecificType(PROPERTY)
            .map { (it.psi as KtProperty).name!! }
        // If refExprs is empty then we assume that it returns some constant
        if (refExprs.isNotEmpty()) {
            handleReferenceExpressions(node, refExprs, propsWithBackSymbol, localProps)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleSetAccessors(
        accessor: ASTNode,
        node: ASTNode,
        propsWithBackSymbol: List<String>) {
        val refExprs = accessor.findAllDescendantsWithSpecificType(REFERENCE_EXPRESSION)

        // In set we don't check for local properties. At least one reference expression should contain field or _prop
        if (refExprs.isNotEmpty()) {
            handleReferenceExpressions(node, refExprs, propsWithBackSymbol, null)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleReferenceExpressions(node: ASTNode,
                                           expressions: List<ASTNode>,
                                           backingPropertiesNames: List<String>,
                                           localProperties: List<String>?) {
        if (expressions.none {
                backingPropertiesNames.contains(it.text) || it.text == "field" || localProperties?.contains(it.text) == true
            }) {
            raiseWarning(node, node.getFirstChildWithType(IDENTIFIER)!!.text)
        }
    }

    private fun raiseWarning(node: ASTNode, propName: String) {
        NO_CORRESPONDING_PROPERTY.warn(configRules, emitWarn, isFixMode,
            "$propName has no corresponding property with name _$propName", node.startOffset, node)
    }
}
