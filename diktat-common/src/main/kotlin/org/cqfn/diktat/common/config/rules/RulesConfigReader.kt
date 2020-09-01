package org.cqfn.diktat.common.config.rules

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.BufferedReader
import java.io.File
import java.util.stream.Collectors
import org.cqfn.diktat.common.config.reader.JsonResourceConfigReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This interface represents individual inspection in rule set.
 */
interface Rule {
    /**
     * @return name of this [Rule]
     */
    fun ruleName(): String
}

/**
 * Configuration of individual [Rule]
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class RulesConfig(
        val name: String,
        val enabled: Boolean,
        val configuration: Map<String, String>
)

/**
 * Configuration that allows customizing additional options of particular rules.
 */
open class RuleConfiguration(protected val config: Map<String, String>)
object EmptyConfiguration : RuleConfiguration(mapOf())

/**
 * class returns the list of configurations that we have read from a json: rules-config.json
 */
open class RulesConfigReader(override val classLoader: ClassLoader) : JsonResourceConfigReader<List<RulesConfig>>() {
    companion object {
        val log: Logger = LoggerFactory.getLogger(RulesConfigReader::class.java)
    }

    /**
     * Parse resource file into list of [RulesConfig]
     *
     * @param fileStream a [BufferedReader] representing loaded rules config file
     * @return list of [RulesConfig]
     */
    override fun parseResource(fileStream: BufferedReader): List<RulesConfig> {
        val mapper = jacksonObjectMapper()
        val jsonValue = fileStream.lines().collect(Collectors.joining())
        return mapper.readValue(jsonValue)
    }

    /**
     * instead of reading the resource as it is done in the interface we will read a file by the absolute path here
     * if the path is provided, else will read the hardcoded file 'rules-config.json' from the package
     *
     * @param resourceFileName name of the resource which will be loaded using [classLoader]
     * @return [BufferedReader] representing loaded resource
     */
    override fun getConfigFile(resourceFileName: String): BufferedReader? {
        val resourceFile = File(resourceFileName)
        return if (resourceFile.exists()) {
            log.debug("Using rules-config.json file from the following path: ${resourceFile.absolutePath}")
            File(resourceFileName).bufferedReader()
        } else {
            log.debug("Using the default rules-config.json file from the class path")
            classLoader.getResourceAsStream(resourceFileName)?.bufferedReader()
        }
    }
}

// ================== utils for List<RulesConfig> from json config

/**
 * Get [RulesConfig] for particular [Rule] object.
 *
 * @param rule a [Rule] which configuration will be returned
 * @return [RulesConfig] for a particular rule if it is found, else null
 */
fun List<RulesConfig>.getRuleConfig(rule: Rule): RulesConfig? = this.find { it.name == rule.ruleName() }

/**
 * checking if in json config particular rule is enabled or disabled
 * (!) the default value is "true" (in case there is no config specified)
 *
 * @param rule a [Rule] which is being checked
 * @return true if rule is enabled in configuration, else false
 */
fun List<RulesConfig>.isRuleEnabled(rule: Rule): Boolean {
    val ruleMatched = getRuleConfig(rule)
    return ruleMatched?.enabled ?: true
}
