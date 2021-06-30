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

fun goo() {
    val ok = true // short comment
    val someLongFieldName = "some string" // h k comment looong comment
}
