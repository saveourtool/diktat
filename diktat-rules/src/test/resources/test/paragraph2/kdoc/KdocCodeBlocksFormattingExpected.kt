package test.paragraph2.kdoc

/**
 * This is a test kDoc Comment
 */
class SomeClass {
/* block comment to func */
    fun testFunc() {
        val a = 5  // Right side comment good
        val c = 6  // Side comment

        when (this) {
            is AnotherClass -> println()
            // Comment
            is Any -> println()
        }

        /* General if comment */
        if (a == 5) {

        }
        else {
// Some Comment
        }

        if (a == 5) {

        }
        else
// Some Comment
            print(a)
        /* Block Comment */
        val some = 4

        /* This is a block comment */

        /*
            Don't fix this comment
        */
    }

    /**
     * Useless kdoc comment
     * Line
     */

    /**
     *     This is a useless function
     */
    fun someUselessFunction() {
// This is a useless value
        val uselessValue = 1
    }

// Class comment
    val b = 6

/* Comment to this useless func*/
    fun anotherUselessFunc() {

    }
}
