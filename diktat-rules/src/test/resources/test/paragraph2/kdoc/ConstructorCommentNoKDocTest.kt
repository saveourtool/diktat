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

class A constructor(
        private var name: String,
        val lastName: String
){}

class A constructor(
        /*
         * hello
         */
        val name: String
){}

class Example(private val foo: Int)

class Example constructor(
        val g: String,
        private var q: String,
        @param:JsonProperty("shortName") private val shortName: String,
        val e: String,
        protected val s: String
){}
