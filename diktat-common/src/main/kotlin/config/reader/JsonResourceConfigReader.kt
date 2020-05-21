package config.reader

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException

/**
 * @param <T> - class name parameter that will be used in calculation of classpath
</T> */
interface JsonResourceConfigReader<T> {
    /**
     * @param resourceFileName - related path to a file from resources
     */
    fun readResource(resourceFileName: String): T? {
        val fileURL = javaClass.classLoader.getResource(resourceFileName)
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

    fun parseResource(file: File): T

    companion object {
        val log: Logger = LoggerFactory.getLogger(JsonResourceConfigReader::class.java)
    }
}
