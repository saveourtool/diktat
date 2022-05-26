/**
 * This rule checks if there is a backing property for field with property accessors, in case they don't use field keyword
 */
class ImplicitBackingPropertyRuleTest(configRules: List<RulesConfig>, prevId: String?) {
    private fun validateAccessors(node: ASTNode, propsWithBackSymbol: List<String>) {
        val accessors = node.findAllDescendantsWithSpecificType(PROPERTY_ACCESSOR).filter { it.hasChildOfType(BLOCK) }  // Comment, which shouldn't be moved
        accessors.filter { it.hasChildOfType(GET_KEYWORD) }.forEach { handleGetAccessors(it, node, propsWithBackSymbol) }
        accessors.filter { it.hasChildOfType(SET_KEYWORD) }.forEach { handleSetAccessors(it, node, propsWithBackSymbol) }
    }
}
