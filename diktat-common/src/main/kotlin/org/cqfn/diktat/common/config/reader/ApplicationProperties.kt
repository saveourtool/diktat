package org.cqfn.diktat.common.config.reader

import com.pinterest.ktlint.core.initKtLintKLogger
import mu.KotlinLogging
import org.slf4j.LoggerFactory

import java.io.IOException
import java.util.Properties

import kotlin.system.exitProcess

/**
 * Base class for working with properties files.
 */
@Suppress("SwallowedException")
open class ApplicationProperties(propertiesFileName: String) {
    /**
     * The [Properties] loaded from a file.
     */
    val properties: Properties = Properties()

    init {
        val propStream = javaClass.classLoader.getResourceAsStream(propertiesFileName)
        propStream?.let {
            try {
                properties.load(it)
            } catch (e: IOException) {
                errorReadingConfig(propertiesFileName)
            }
        }
            ?: errorReadingConfig(propertiesFileName)
    }

    private fun errorReadingConfig(propertiesFileName: String) {
        log.error("Cannot read file $propertiesFileName with configuration properties")
        exitProcess(EXIT_STATUS_MISSING_PROPERTIES)
    }

    companion object {
        private val log = KotlinLogging.logger(
            LoggerFactory.getLogger(ApplicationProperties::class.java)
        ).initKtLintKLogger()
        private const val EXIT_STATUS_MISSING_PROPERTIES = 4
    }
}
