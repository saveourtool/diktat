package test.paragraph6

open class Rectangle {
    open fun draw() { /* ... */ }
}

class Square() : Rectangle() {
    }

class Square2() : Rectangle() {
    }

class Square2() : Rectangle() {
    override fun draw() {
        val q = super.draw()
    }
}

class A: Runnable {
    override fun run() {

    }
}
