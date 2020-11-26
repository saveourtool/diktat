package org.cqfn.diktat

import org.slf4j.LoggerFactory

import java.io.IOException
import java.util.Properties

import kotlin.system.exitProcess

/**
 * @property foo
 * @property bar
 */
@ExperimentalStdlibApi public data class Example(val foo: Int, val bar: Double) : SuperExample("lorem ipsum")

private class TestException : Exception()
/* this class is unused */
// private class Test : RuntimeException()

private fun foo(node: ASTNode) {
    when (node.elementType) {
        CLASS, FUN, PRIMARY_CONSTRUCTOR, SECONDARY_CONSTRUCTOR -> checkAnnotation(node)
        else -> {
            // this is a generated else block
        }
    }
    val qwe = a && b
    val qwe = a &&
            b

    // comment
    if (x) {
        foo()
    }
}

