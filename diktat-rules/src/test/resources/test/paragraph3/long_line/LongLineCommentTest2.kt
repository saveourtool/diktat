/**
 * This rule checks if there is a backing property for field with property accessors, in case they don't use field keyword
 */
class ImplicitBackingPropertyRuleTest(configRules: List<RulesConfig>) {
    private fun validateAccessors(node: ASTNode, propsWithBackSymbol: List<String>) {
        val accessors = node.findAllDescendantsWithSpecificType(PROPERTY_ACCESSOR).filter { it.hasChildOfType(BLOCK) }  // Comment, which should be moved
        val accessors2 =
            node.findAllDescendantsWithSpecificType(PROPERTY_ACCESSOR).filter { it.hasChildOfType(BLOCK) }  // Comment, which should be moved
        var accessors3 = node.findAllDescendantsWithSpecificType(PROPERTY_ACCESSOR).filter { it.hasChildOfType(BLOCK) }.forEach { handleGetAccessors(it, node, propsWithBackSymbol) }  // Comment, which shouldn't be moved
        var accessors4 = node.findAllDescendantsWithSpecificType(PROPERTY_ACCESSOR).filter { it.hasChildOfType(BLOCK) }.forEach { handleGetAccessors(it, node, propsWithBackSymbol) }  //                                 Comment, which should be moved
        var accessors5 = node.findAllDescendantsWithSpecificType(PROPERTY_ACCESSOR).filter { it.hasChildOfType(BLOCK) }.forEach { handleGetAccessors(it, node, propsWithBackSymbol) }  //                                                                      Comment, which shouldn't be moved
        accessors.filter { it.hasChildOfType(GET_KEYWORD) }.forEach { handleGetAccessors(it, node, propsWithBackSymbol) }
        accessors.filter { it.hasChildOfType(SET_KEYWORD) }.forEach { handleSetAccessors(it, node, propsWithBackSymbol) }
    }
}
