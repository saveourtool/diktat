package test.paragraph3.newlines

fun foo(list: List<Bar>?) {
    list!!.filterNotNull().map { it.baz() }.firstOrNull {
        it.condition()
    }?.qux()
}
