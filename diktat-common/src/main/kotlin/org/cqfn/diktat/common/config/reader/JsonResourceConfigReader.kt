package org.cqfn.diktat.common.config.reader

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.File
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
    abstract val classLoader: ClassLoader

    /**
     * @param resourceFileName - related path to a file from resources
     */
    fun readResource(resourceFileName: String): T? {
        val resourceStream = getConfigFile(resourceFileName)
        if (resourceStream == null) {
            log.error("Not able to open file $resourceFileName from the resources")
        } else {
            try {
                return parseResource(resourceStream)
            } catch (e: IOException) {
                log.error("Cannot read config file $resourceFileName due to: ", e)
            }
        }
        return null
    }

    /**
     * you can override this method in case you would like to read a file not simply from resources
     */
    protected open fun getConfigFile(resourceFileName: String): BufferedReader? {
        return classLoader.getResourceAsStream(resourceFileName)?.bufferedReader()
    }

    /**
     * you can specify your own parser, in example for parsing stream as a json
     */
    protected abstract fun parseResource(fileStream: BufferedReader): T

    companion object {
        val log: Logger = LoggerFactory.getLogger(JsonResourceConfigReader::class.java)
    }
}
