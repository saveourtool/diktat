package test.paragraph3.sort_error

class Test {
    companion object {
        private const val D = 4
        private const val C = 4
        private const val B = 4
        private val SIMPLE_VALUE = listOf(IDENTIFIER, WHITE_SPACE, COMMA, SEMICOLON)
    }
}

class Test2 {
    companion object {
        private const val A = 4
        private const val D = 4
        private val SIMPLE_VALUE = listOf(IDENTIFIER, WHITE_SPACE, COMMA, SEMICOLON)
        private const val B = 4
    }
}

class Test3 {
    companion object
}

class Test2 {
    companion object {
        private const val A = 4
        private const val D = 4
        private val SIMPLE_VALUE = listOf(IDENTIFIER, WHITE_SPACE, COMMA, SEMICOLON)
        private const val Baa = 4
        private const val Ba = 4
        private const val Bb = 4
    }
}
