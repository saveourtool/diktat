package test.paragraph2.kdoc

/**
 * comment
 */
class A constructor(
        val name: String
){}

/**
         * hello
         */
class A constructor(
        val name: String
){}

class A constructor(
        /**
         * text
         */
        @param:JsonProperty("shortName") private val shortName: String
){}
