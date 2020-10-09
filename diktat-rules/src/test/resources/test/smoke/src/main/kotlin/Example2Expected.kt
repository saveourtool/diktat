/*
 * Copyright (c) Your Company Name Here. 2010-2020
 */

package your.name.here

import org.slf4j.LoggerFactory

import java.io.IOException
import java.util.Properties

import kotlin.system.exitProcess

@ExperimentalStdlibApi public data class Example(val foo: Int, val bar: Double) : SuperExample("lorem ipsum")

private class TestException : Exception()

