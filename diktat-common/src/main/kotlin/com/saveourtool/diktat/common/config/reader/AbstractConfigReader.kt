package org.cqfn.diktat.common.config.reader

import mu.KotlinLogging
import java.io.IOException
import java.io.InputStream
import kotlin.jvm.Throws

/**
 * This class is used to read input stream in any format that you will specify.
 * Usage:
 * 1) implement this class with implementing the method:
 *    a. parse - implement parser for your file format (for example parse it to a proper json)
 * 2) Use your new class MyReader().read(someInputStream)
 *
 * @param T - class name parameter that will be used in calculation of classpath
 */
abstract class AbstractConfigReader<T : Any> {
    /**
     * @param inputStream - input stream
     * @return object of type [T] if resource has been parsed successfully
     */
    fun read(inputStream: InputStream): T? = try {
        parse(inputStream)
    } catch (e: IOException) {
        log.error("Cannot read config from input stream due to: ", e)
        null
    }

    /**
     * you can specify your own parser, in example for parsing stream as a json
     *
     * @param inputStream a [InputStream] representing loaded content
     * @return resource parsed as type [T]
     * @throws IOException
     */
    @Throws(IOException::class)
    protected abstract fun parse(inputStream: InputStream): T

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
