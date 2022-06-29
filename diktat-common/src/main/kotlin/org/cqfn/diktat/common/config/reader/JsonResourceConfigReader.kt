package org.cqfn.diktat.common.config.reader

import com.pinterest.ktlint.core.initKtLintKLogger
import mu.KotlinLogging
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.io.BufferedReader
import java.io.IOException

/**
 * This class is used to read some resource in any format that you will specify.
 * Usage:
 * 1) implement this class with implementing two methods:
 *    a. getConfigFile - to return URI (path) to the file that you want to read
 *    b. parseResource - implement parser for your file format (for example parse it to a proper json)
 * 2) Use your new class MyReader(javaClass.classLoader).readResource("some/path/to/file.format")
 *
 * @param <T> - class name parameter that will be used in calculation of classpath
 */
abstract class JsonResourceConfigReader<T> {
    /**
     * The [ClassLoader] used to load the requested resource.
     */
    abstract val classLoader: ClassLoader

    /**
     * @param resourceFileName - related path to a file from resources
     * @return object of type [T] if resource has been parsed successfully
     */
    fun readResource(resourceFileName: String): T? {
        val resourceStream = getConfigFile(resourceFileName)
        resourceStream?.let {
            try {
                return parseResource(it)
            } catch (e: IOException) {
                log.error("Cannot read config file $resourceFileName due to: ", e)
            }
        }
            ?: log.error("Not able to open file $resourceFileName from the resources")
        return null
    }

    /**
     * you can override this method in case you would like to read a file not simply from resources
     *
     * @param resourceFileName name of the resource which will be loaded using [classLoader]
     * @return [BufferedReader] representing loaded resource
     */
    protected open fun getConfigFile(resourceFileName: String): BufferedReader? =
        classLoader.getResourceAsStream(resourceFileName)?.bufferedReader()

    /**
     * you can specify your own parser, in example for parsing stream as a json
     *
     * @param fileStream a [BufferedReader] representing loaded resource file
     * @return resource parsed as type [T]
     */
    protected abstract fun parseResource(fileStream: BufferedReader): T

    companion object {
        /**
         * A [Logger] that can be used
         */
        val log: Logger = KotlinLogging.logger(
            LoggerFactory.getLogger(JsonResourceConfigReader::class.java)
        ).initKtLintKLogger()
    }
}
