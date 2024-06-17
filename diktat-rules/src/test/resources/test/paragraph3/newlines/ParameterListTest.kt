package test.paragraph3.newlines

fun bar(arg1: Int, arg2: Int, arg3: Int) { }

class Foo : FooBase<Bar>(), BazInterface, BazSuperclass { }

class Foo(val arg1: Int, arg2: Int) { }

class Foo(val arg1: Int, arg2: Int, arg3: Int) {
    constructor(arg1: Int, arg2: String, arg3: String) : this(arg1, 0, 0) { }
}

class Foo(val arg1: Int,
          var arg2: Int,
          arg3: Int) { }
