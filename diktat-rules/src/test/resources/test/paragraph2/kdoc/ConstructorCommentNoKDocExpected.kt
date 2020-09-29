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
 * @property name
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
