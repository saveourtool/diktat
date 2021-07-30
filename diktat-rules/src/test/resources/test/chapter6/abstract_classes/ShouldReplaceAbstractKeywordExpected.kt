package test.paragraph6.abstract_classes

actual open class CoroutineTest actual constructor() {
    actual fun <T> runTest(block: suspend CoroutineScope.() -> T) {
        runBlocking {
            block()
        }
    }
}

open class Some() {
    fun some(){}

    fun another(){}

    @SomeAnnotation @Another open inner class Any {
        fun func(){}
    }

    inner open class Second {
        fun someFunc(){}
    }
}

abstract class Another {
    abstract fun absFunc()

    fun someFunc(){}
}
