package org.cqfn.diktat.common.config.reader

import mu.KotlinLogging

import java.io.IOException
import java.io.InputStream

/**
 * This class is used to read some resource in any format that you will specify.
 * Usage:
 * 1) implement this class with implementing two methods:
 *    a. getConfigFile - to return URI (path) to the file that you want to read
 *    b. parse - implement parser for your file format (for example parse it to a proper json)
 * 2) Use your new class MyReader(javaClass.classLoader).readResource("some/path/to/file.format")
 *
 * @property classLoader The [ClassLoader] used to load the requested resource.
 * @param T - class name parameter that will be used in calculation of classpath
 */
abstract class AbstractResourceConfigReader<T : Any>(
    protected val classLoader: ClassLoader
) : AbstractConfigReader<T>() {
    /**
     * @param resourceFileName - related path to a file from resources
     * @return object of type [T] if resource has been parsed successfully
     */
    fun readResource(resourceFileName: String): T? {
        val resourceStream = getConfigFile(resourceFileName)
        resourceStream?.let {
            try {
                return parse(it)
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
     * @return [InputStream] representing loaded resource
     */
    protected open fun getConfigFile(resourceFileName: String): InputStream? = classLoader.getResourceAsStream(resourceFileName)

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
