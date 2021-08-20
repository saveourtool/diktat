package test.paragraph1.naming.identifiers

/**
 * @property anAbcMember
 * @property anotherAbcMember
 * @property anDefMember
 * @property anotherDefMember
 */
data class Abc(
    val anAbcMember: String,
    val anotherAbcMember: String,
) {
    private val anDefMember: String = ""
    private val anotherDefMember: String = ""
}
