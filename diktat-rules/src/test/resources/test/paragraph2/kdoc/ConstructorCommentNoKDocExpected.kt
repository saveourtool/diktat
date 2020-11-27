package test.paragraph2.kdoc

/**
 * @property name comment
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

/**
         * text
         */
class A constructor(
        @param:JsonProperty("shortName") private val shortName: String
){}

/**
 * @property lastName
 */
class A constructor(
        private var name: String,
        val lastName: String
){}

/**
 * @property name
         * hello
          */
class A constructor(
        val name: String
){}

class Example(private val foo: Int)

/**
 * @property g
  * @property e
 * @property s
*/
class Example constructor(
        val g: String,
        private var q: String,
        @param:JsonProperty("shortName") private val shortName: String,
        val e: String,
        protected val s: String
){}
