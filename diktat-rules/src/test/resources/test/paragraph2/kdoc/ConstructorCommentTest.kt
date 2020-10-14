package test.paragraph2.kdoc

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

/**
 * @property name another text
 */
class A (
        /*
         * text
         */
        val name: String
){}
