package test.paragraph3.indentation

class Example(protected val property1: Type1,
              private val property2: Type2,
              property3: Type3) {
    constructor(property1: Type1,
                property2: Type2) : this(property1,
            property2, defaultValue)
}

