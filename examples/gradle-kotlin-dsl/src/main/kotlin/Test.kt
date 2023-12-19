package incorrect

class IncorrectnameException : Exception() {
    // fun myCommentedFunction() {
    // }

    val Incorrect_Val = 5

    /**
     * @throws Exception
     */
    fun incorrectFunction() {
        throw Exception()
    }
}
