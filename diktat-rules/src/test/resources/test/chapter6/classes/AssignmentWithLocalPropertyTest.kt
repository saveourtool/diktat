package test.chapter6.classes

class Foo {
    val a: Int

    constructor(a: Int) {
        val f = F(a)
        this.a = f.foo()
    }
}