package test.paragraph3.long_line

fun foo() {
    val veryLooongStringName = "ASDFGHJKL"
    val veryLooooooongConstIntName1 = 12345
    val veryLooooooongConstIntName2 = 54321
    var carry = 1
    if (veryLooooooongConstIntName1 > veryLooooooongConstIntName2) {
        carry++
    } else if (veryLooooooongConstIntName2 > 123 * 12 && veryLooongStringName != "asd") {
        carry+=2
    } else if (1234 + 1235 + 1236 + 1237 + 1238 > 124 * 12) {
        carry+=3
    }
}