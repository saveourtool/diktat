package test.paragraph3.collapse_if


fun foo() {
    if (true && true && true) {
        val a = 6
    }
}

fun foo() {
    if (true) {
        val someConstant = 5
        if (true) {
            doSomething()
        }
    }
}

fun foo() {
    if (true && true) {

        doSomething()
    }
}

fun foo() {
     if (cond1 && cond2) {
         firstAction()
secondAction()
         if (cond3) {
             secondAction()
         }
     }
}

fun foo() {
    if (true && true && true && true && true && true) {
        doSomething()
    }
}

fun foo() {

}

fun foo() {
    fun1()
    if (cond1) {
        fun2()
    } else if (cond2) {
        fun3()
    } else {
        fun4()
    }
}

fun foo() {
    fun1()
    if (cond1) {
        fun2()
    } else if (cond2 && true) {
        fun3()
    } else {
        fun4()
    }
}

fun foo() {
    fun1()
    if (cond1) {
        fun2()
    } else if (cond2 && true && true) {
        fun3()
    } else {
        fun4()
    }
}

fun foo() {
    fun1()
    if (cond1) {
        fun2()
    } else if (cond2 && true && true) {
        fun3()
    } else {
        fun4()
        if (true && true) {
            fun5()
        }
    }
}