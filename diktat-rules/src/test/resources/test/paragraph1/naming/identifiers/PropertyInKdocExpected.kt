package test.paragraph1.naming.identifiers

/**
 * @property anAbcMember
 * @property another_abc_member
 * @property anDefMember
 * @property anotherDefMember
 */
data class Abc(
    private val anAbcMember: String,
    val another_abc_member: String,
) {
    private val anDefMember: String = ""
    private val anotherDefMember: String = ""
}
