package test.paragraph6.`useless-override`

open class A {
    open fun foo(){}
}

interface C {
    fun foo(){}
    fun goo(){}
}

class B: A(){
    override fun foo() {
        super.foo()
    }
}

class D: A(), C {
    override fun foo() {
        super<C>.foo()
        super<A>.foo()
        super.goo()
    }

    private fun qwe(){
        val q = super.goo()
    }
}
