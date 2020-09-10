package org.cqfn.diktat.common.config.reader

import java.io.IOException
import java.util.Properties
import kotlin.system.exitProcess
import org.slf4j.LoggerFactory

/**
 * Base class for working with properties files.
 */
open class ApplicationProperties(propertiesFileName: String) {
    /**
     * The [Properties] loaded from a file.
     */
    val properties: Properties = Properties()

    init {
        val propStream = javaClass.classLoader.getResourceAsStream(propertiesFileName)
        if (propStream != null) {
            try {
                properties.load(propStream)
            } catch (e: IOException) {
                errorReadingConfig(propertiesFileName)
            }
        } else {
            errorReadingConfig(propertiesFileName)
        }
    }

    private fun errorReadingConfig(propertiesFileName: String) {
        log.error("Cannot read file $propertiesFileName with configuration properties")
        exitProcess(EXIT_STATUS_MISSING_PROPERTIES)
    }

    companion object {
        private val log = LoggerFactory.getLogger(ApplicationProperties::class.java)
        private const val EXIT_STATUS_MISSING_PROPERTIES = 4
    }
}
