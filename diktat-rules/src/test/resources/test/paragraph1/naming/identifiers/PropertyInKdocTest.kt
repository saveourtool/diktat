package test.paragraph1.naming.identifiers

/**
 * @property an_abc_member
 * @property another_abc_member
 * @property an_def_member
 * @property another_def_member
 */
data class abc(
    private val an_abc_member: String,
    val another_abc_member: String,
) {
    private val an_def_member: String = ""
    private val another_def_member: String = ""
}
