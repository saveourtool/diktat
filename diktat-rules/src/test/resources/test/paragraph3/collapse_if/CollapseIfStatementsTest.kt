package test.paragraph3.collapse_if


fun foo() {
    if (true) {
        if (true) {
            if (true) {
                val a = 6
            }
        }
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
    if (true) {

        if (true) {
            doSomething()
        }
    }
}

fun foo() {
    if (true) {
        if (true) {
            if (true) {
                if (true) {
                    if (true) {
                        if (true) {
                            doSomething()
                        }
                    }
                }
            }
        }
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
    } else if (cond2) {
        if (true) {
            fun3()
        }
    } else {
        fun4()
    }
}

fun foo() {
    fun1()
    if (cond1) {
        fun2()
    } else if (cond2) {
        if (true) {
            if (true) {
                fun3()
            }
        }
    } else {
        fun4()
    }
}

fun foo() {
    fun1()
    if (cond1) {
        fun2()
    } else if (cond2) {
        if (true) {
            if (true) {
                fun3()
            }
        }
    } else {
        fun4()
        if (true) {
            if (true) {
                fun5()
            }
        }
    }
}

fun foo() {
    if (cond1) {
        if (cond2 && cond3 || cond4) {
            firstAction()
            secondAction()
        }
    }
}

fun foo() {
    if (cond1) {
        if (cond2) {
            if (cond3 || cond4) {
                someAction()
            }
        }
    }
}

fun foo() {
    if (cond1) {
        if (cond2) {
            firstAction()
            secondAction()
        } else {
            firstAction()
        }
    } else {
        secondAction()
    }
}

fun foo () {
    if (cond1) {
        if (cond2) {
            firstAction()
            secondAction()
        } else if (cond3) {
            firstAction()
        } else {
            val a = 5
        }
    } else {
        secondAction()
    }
}

fun foo() {
    if (cond1) {
        if (cond2) {
            firstAction()
            secondAction()
        }
    }
    if (cond3) {
        secondAction()
    }
}

fun foo() {
    if (cond1) {
        if (cond2 || cond3) {
            firstAction()
            secondAction()
        }
    }
    if (cond4) {
        secondAction()
    }
}

fun foo () {
    if (cond1) {
        if (cond2 || cond3) {
            firstAction()
            secondAction()
        }
        if (cond4) {
            secondAction()
        }
    }
}

fun foo() {
    if (cond1) {
        if (cond2) {
            doSomething()
        }
        val a = 5
    }
}

fun foo() {
    if (true) {
        /*
         Some Comments
        */
        // More comments
        if (true) {
            // comment 1
            val a = 5
            // comment 2
            doSomething()
        }
        // comment 3
    }
}

fun foo() {
    if (true) {
        // Some
        // comments
        if (true) {
            doSomething()
        }
    }
}

fun foo() {
    // comment
    if (cond1) {
        /*
         Some comments
        */
        // More comments
        if (cond2 || cond3) {
            doSomething()
        }
    }
}

fun foo() {
    if (cond1) {
        // comment
        if (cond2) {
            // comment 2
            if (cond3) {
                doSomething()
            }
        }
    }
}

fun foo () {
    if (true) {
        if (true) {doSomething()}
    }
}

fun foo () {
     if (/*comment*/ true) {
         if (true) {
             doSomething()
         }
     }
}

fun foo () {
    if (true /*comment*/) {
        if (true) {
            doSomething()
        }
    }
}

fun foo () {
    if (true) {
        if (true /*comment*/) {
            doSomething()
        }
    }
}

fun foo () {
    if (true && (true || false) /*comment*/) {
        if (true /*comment*/) {
            doSomething()
        }
    }
}

fun foo () {
     if (true
     /*comment
     * more comments
     */
     ) {
         if (true /*comment 2*/) {
             doSomething()
         }
     }
}
