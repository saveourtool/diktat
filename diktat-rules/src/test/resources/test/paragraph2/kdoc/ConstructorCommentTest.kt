package test.paragraph2.kdoc

/**
 * Simple class
 */
class A constructor(
        // should replace
        val name: String
)

/**
 * @property age age
 */
class A(
        val age: Int,
        //Ivanov
        val lastName: String
) {

}

class Out{
    /**
     * Inner class
     */
    class In(
            //Jack
           val name: String
    ){}
}

/**
 *
 */
class A (
        /**
         * some text
         */
        var name: String
){}

/**
 * @property name another text
 */
class A (
        /**
         * some text
         */
        var name: String
){}

/**
 * @property name another text
 */
class A (
        /**
         * @property name some text
         */
        val name: String
){}
