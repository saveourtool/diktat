package test.paragraph6.abstract_classes

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