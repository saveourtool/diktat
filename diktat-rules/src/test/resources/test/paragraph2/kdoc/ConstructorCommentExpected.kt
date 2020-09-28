package test.paragraph2.kdoc

/**
 * Simple class
 * @property name  should replace
*/
class A constructor(
        val name: String
)

/**
 * @property age age
 * @property lastName Ivanov
*/
class A(
        val age: Int,
        val lastName: String
) {

}

class Out{
    /**
     * Inner class
     * @property name Jack
*/
    class In(
            val name: String
    ){}
}

/**
 *
 * some text
*/
class A (
        var name: String
){}

/**
 * @property name another text
 * some text
*/
class A (
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
