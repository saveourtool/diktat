package org.cqfn.diktat.common.config.rules

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.cqfn.diktat.common.config.reader.JsonResourceConfigReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.IllegalArgumentException
import java.net.URL

interface Rule {
    fun ruleName(): String
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class RulesConfig(
    val name: String,
    val enabled: Boolean,
    val configuration: Map<String, String>
)

open class RuleConfiguration(protected val config: Map<String, String>)
object EmptyConfiguration: RuleConfiguration(mapOf())

/**
 * class returns the list of configurations that we have read from a json: rules-config.json
 */
class RulesConfigReader : JsonResourceConfigReader<List<RulesConfig>>() {
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
        return File(resourceFileName).toURI().toURL()
    }
}


// ================== utils for List<RulesConfig> from json config

fun List<RulesConfig>.getRuleConfig(rule: Rule): RulesConfig? {
    return this.find { it.name == rule.ruleName() }
}

/**
 * checking if in json config particular rule is enabled or disabled
 * (!) the default value is "true" (in case there is no config specified)
 */
fun List<RulesConfig>.isRuleEnabled(rule: Rule): Boolean {
    val ruleMatched = getRuleConfig(rule)
    return ruleMatched?.enabled ?: true
}
