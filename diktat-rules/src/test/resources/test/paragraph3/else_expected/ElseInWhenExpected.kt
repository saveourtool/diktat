package test.paragraph3.else_expected

enum class TestEnum {
    ONE, TWO
}

fun testWhenExpression() {
    val directoryType = TestEnum.ONE

    when (directoryType) {
        TestEnum.ONE -> "d"
        TestEnum.TWO -> "-"
else -> {
}
}

    val noElse = when (directoryType) {
        TestEnum.ONE -> "d"
        TestEnum.TWO -> "a"
    }
}
