package test.paragraph3.block_brace

fun divideOrZero(numerator: Int, denominator: Int): Int {
    try { return numerator / denominator
    } catch (e: ArithmeticException) {
        return 0
    }
    catch (e: Exception) {
        return 1
    }
    finally {
        println("Hello") }
}