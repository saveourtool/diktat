package test.paragraph2.kdoc

class A constructor(
        //comment
        val name: String
){}

class A constructor(
        /**
         * hello
         */
        val name: String
){}

class A constructor(
        /**
         * text
         */
        @param:JsonProperty("shortName") private val shortName: String
){}
