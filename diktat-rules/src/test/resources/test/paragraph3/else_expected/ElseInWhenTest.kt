package test.paragraph3.else_expected

enum class TestEnum {
    ONE, TWO
}

fun testWhenExpression() {
    val directoryType = TestEnum.ONE

    when (directoryType) {
        TestEnum.ONE -> "d"
        TestEnum.TWO -> "-"
    }

    val noElse = when (directoryType) {
        TestEnum.ONE -> "d"
        TestEnum.TWO -> "a"
    }

    val v = 1
    when (v) {
        f(TestEnum.ONE) -> print("1")
        f(TestEnum.TWO) -> print("2")
    }

    val inLambda = {x: Int -> when(x) {
        1 -> print(5)
    }
    }
}

sealed class Expr {
    class Num(val value: Int) : Expr()
    class Sum(val left: Expr, val right: Expr) : Expr()
}
fun eval(e: Expr): Int =
    when (e) {
        is Expr.Num -> e.value
        is Expr.Sum -> eval(e.right) + eval(e.left)
    }

