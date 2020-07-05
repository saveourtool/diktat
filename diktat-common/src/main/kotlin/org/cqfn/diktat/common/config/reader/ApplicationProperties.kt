package common

import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*
import kotlin.system.exitProcess

open class ApplicationProperties(propertiesFileName: String) {
    val properties: Properties = Properties()
    private fun errorReadingConfig(propertiesFileName: String) {
        log.error("Cannot read file $propertiesFileName with configuration properties")
        exitProcess(EXIT_STATUS_MISSING_PROPERTIES)
    }

    companion object {
        private val log = LoggerFactory.getLogger(ApplicationProperties::class.java)
        private const val EXIT_STATUS_MISSING_PROPERTIES = 4
    }

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
}
