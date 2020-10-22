package test.paragraph4.smart_cast

class Some {
    val x = ""

    fun someFunc() {
        if (x is String) {
            print(x.length)
            var a = x
        }
    }
}
