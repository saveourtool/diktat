package test.paragraph6

open class Rectangle {
    open fun draw() { /* ... */ }
}

class Square() : Rectangle() {
    override fun draw() {
        /**
         *
         * hehe
         */
        super.draw()
    }
}

class Square2() : Rectangle() {
    override fun draw() {
        //hehe
        /*
            hehe
        */
        super.draw()
    }
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
