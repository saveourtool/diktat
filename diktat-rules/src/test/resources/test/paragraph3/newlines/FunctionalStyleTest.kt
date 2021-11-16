package test.paragraph3.newlines

fun foo(list: List<Bar>?) {
    list!!.filterNotNull().map { it.baz() }.firstOrNull {
        it.condition()
    }?.qux()
            ?:
            foobar
}

fun bar(x :Int,y:Int) :Int {
    return   x+ y }

fun goo() {
    x.map().gro()
            .gh()
    t.map().hg().hg()
    t
            .map()
            .filter()
            .takefirst()
    x
            .map()
            .filter().hre()

    t.responseBody!![0].
    name
}

fun foo() {
    foo ?: bar.baz().qux()

    foo ?: bar.baz()
            .qux()
}

fun controlFlow(code: CodeBlock, format: String, vararg args: Any?): CodeBlock =
    CodeBlock.builder().beginControlFlow(format, *args).add(code)
        .endControlFlow().build()

fun controlFlow(code: CodeBlock, format: String, vararg args: Any?,): CodeBlock =
    CodeBlock.builder().beginControlFlow(format, *args).add(code)
        .endControlFlow().build()

fun foo(a: Int, b: Int, c: Int): Int = 42
