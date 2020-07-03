package org.cqfn.diktat.common.config.reader

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.net.URL

/**
 * @param <T> - class name parameter that will be used in calculation of classpath
</T> */
abstract class JsonResourceConfigReader<T> {
    /**
     * @param resourceFileName - related path to a file from resources
     */
    fun readResource(resourceFileName: String): T? {
        val fileURL = getConfigFile(resourceFileName)
        if (fileURL == null) {
            log.error("Not able to open file $resourceFileName from the resources")
        } else {
            val resource = File(fileURL.file)
            try {
                return parseResource(resource)
            } catch (e: IOException) {
                log.error("Cannot read json-file $resourceFileName due to: ", e)
            }
        }
        return null
    }

    protected open fun getConfigFile(resourceFileName: String): URL? {
        return javaClass.classLoader.getResource(resourceFileName)
    }

    protected abstract fun parseResource(file: File): T

    companion object {
        val log: Logger = LoggerFactory.getLogger(JsonResourceConfigReader::class.java)
    }
}
