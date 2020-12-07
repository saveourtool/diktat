package test.paragraph3.newlines

fun foo(list: List<Bar>?) {
    list!!
.filterNotNull()
.map { it.baz() }
.firstOrNull {
        it.condition()
    }
?.qux()
            ?:foobar
}

fun bar(x :Int,y:Int) = x+ y

fun foo() {
    foo
            ?: bar
                    .baz()
                    .qux()
}
