package test.chapter6.stateless_classes

interface I {
    fun foo()
}

object O: I {
    override fun foo() {}
}