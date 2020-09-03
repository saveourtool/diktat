package test.paragraph3.spaces

class Example<T, R, Q> where T : UpperType, R : UpperType, Q : UpperType {
    fun foo(t: T) = t + 1
    fun foo2(t: T) = t + 1
    fun foo3(t: T) = t + 1

    fun bar() {
        listOf<T>().map(this::foo)?.filter { elem -> predicate(elem) }!!
        listOf<T>().map(this::foo)?.filter { elem -> predicate(elem) }!!.first()
        listOf<T>().map(this::foo)?.filter { elem -> predicate(elem) }!!.first()
    }
}

class Test(@field:Anno val foo: Type,
           @get:Anno val bar: Type,
           @param:Anno val baz: Type) {
    fun foo(): String = "lorem"
    fun bar(): String = "ipsum"
    fun baz(): String = "dolor"
}
