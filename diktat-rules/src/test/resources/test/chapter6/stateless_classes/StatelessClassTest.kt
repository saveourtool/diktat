package test.chapter6.stateless_classes

interface I {
    fun foo()
}

class O: I {
    override fun foo() {}
}

/**
 * Some KDOC
 */
class A: I {
    override fun foo() {}
}
