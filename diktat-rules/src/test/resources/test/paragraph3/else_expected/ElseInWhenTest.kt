package test.paragraph3.else_expected

enum class TestEnum {
    ONE, TWO
}

fun testWhenExpression() {
    val directoryType = TestEnum.ONE

    val objectType = when (directoryType) {
        TestEnum.ONE -> "d"
        TestEnum.TWO -> "-"
    }
}
