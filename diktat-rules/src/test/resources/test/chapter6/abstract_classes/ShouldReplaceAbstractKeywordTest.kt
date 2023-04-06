package test.paragraph6.abstract_classes

actual abstract class CoroutineTest actual constructor() {
    actual fun <T> runTest(block: suspend CoroutineScope.() -> T) {
        runBlocking {
            block()
        }
    }
}

abstract class Some() {
    fun some(){}

    fun another(){}

    @SomeAnnotation @Another abstract inner class Any {
        fun func(){}
    }

    inner abstract class Second {
        fun someFunc(){}
    }
}

abstract class Another {
    abstract fun absFunc()

    fun someFunc(){}
}
