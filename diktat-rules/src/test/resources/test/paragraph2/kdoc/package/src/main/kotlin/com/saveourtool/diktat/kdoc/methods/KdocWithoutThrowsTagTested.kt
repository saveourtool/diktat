package test.paragraph2.kdoc.`package`.src.main.kotlin.com.saveourtool.diktat.kdoc.methods

/**
 * @param onSuccess
 * @param onFailure
 */
fun parseInputNumber(onSuccess: (number: Int) -> Unit, onFailure: () -> Unit) {
    try {
        val input: Int = binding.inputEditText.text.toString().toInt()
        if (input < 0)
            throw NumberFormatException()
        throw ArrayIndexOutOfBounds()
        throw NullPointerException()

        onSuccess(input)
    } catch (e: IllegalArgumentException) {
        onFailure()
    } catch (e: NullPointerException) {
        onFailure()
    }
}
