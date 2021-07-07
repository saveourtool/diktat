package test.paragraph2.kdoc

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

/**
 * @property name another text
* text
 */
class A (
        val name: String
){}
