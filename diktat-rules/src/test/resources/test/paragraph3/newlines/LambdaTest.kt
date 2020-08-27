package test.paragraph3.newlines

class Example {
    val a = list.map {
        elem ->
        foo(elem)
    }
    val b = list.map { elem: Type
        ->
        foo(elem)
    }
    val c = list.map { elem
        -> bar(elem)
    }
    val d = list.map { elem: Type -> bar(elem)
        foo(elem)
    }
    val e = list.map { bar(elem)
        foo(elem)
    }
}
