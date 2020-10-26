package test.paragraph6.abstract_classes

abstract class Some() {
    fun some(){}

    fun another(){}

    abstract inner class Any {
        fun func(){}
    }
}

abstract class Another {
    abstract fun absFunc()

    fun someFunc(){}
}