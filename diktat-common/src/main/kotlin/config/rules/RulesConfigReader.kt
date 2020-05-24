package config.rules

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import config.reader.JsonResourceConfigReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URL

interface Rule {
    fun text(): String
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class RulesConfig(
    var name: String,
    var enabled: Boolean,
    var configuration: String
)

/**
 * class returns the list of configurations that we have read from a json: rules-config.json
 */
class RulesConfigReader : JsonResourceConfigReader<List<RulesConfig>> {
    companion object {
        val log: Logger = LoggerFactory.getLogger(RulesConfigReader::class.java)
    }

    override fun parseResource(file: File): List<RulesConfig> {
        val mapper = jacksonObjectMapper()
        return if (file.exists()) mapper.readValue(file) else {
            log.error("Cannot read json configuration of rules - file ${file.absolutePath} does not exist")
            emptyList()
        }
    }

    /**
     * instead of reading the resource as it is done in the interface we will read a file by the absolute path here
     * if the path is provided, else will read the hardcoded file 'rules-config.json' from the package
     */
    override fun getConfigFile(resourceFileName: String): URL? {
        return File(resourceFileName).toURL()
    }
}

fun List<RulesConfig>.getRuleConfig(rule: Rule): RulesConfig? {
    return this.find { it.name == rule.text()}
}
