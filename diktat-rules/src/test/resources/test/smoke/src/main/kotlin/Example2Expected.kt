package org.cqfn.diktat

/**
 * @property foo
 * @property bar
 */
@ExperimentalStdlibApi public data class Example(val foo: Int, val bar: Double) : SuperExample("lorem ipsum")

private class TestException : Exception()

