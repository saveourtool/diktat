/**
 * Classes and extensions needed to read and parse rules configuration file
 */

package org.cqfn.diktat.common.config.rules

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.BufferedReader
import java.io.File
import org.cqfn.diktat.common.config.reader.JsonResourceConfigReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory

const val DIKTAT_COMMON = "DIKTAT_COMMON"

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
        val enabled: Boolean = true,
        val configuration: Map<String, String>
)

/**
 * Configuration that allows customizing additional options of particular rules.
 */
open class RuleConfiguration(protected val config: Map<String, String>)
object EmptyConfiguration : RuleConfiguration(mapOf())

/**
 * class returns the list of configurations that we have read from a yml: diktat-analysis.yml
 */
open class RulesConfigReader(override val classLoader: ClassLoader) : JsonResourceConfigReader<List<RulesConfig>>() {
    /**
     * Parse resource file into list of [RulesConfig]
     *
     * @param fileStream a [BufferedReader] representing loaded rules config file
     * @return list of [RulesConfig]
     */
    override fun parseResource(fileStream: BufferedReader): List<RulesConfig> {
        val mapper = ObjectMapper(YAMLFactory())
        mapper.registerModule(KotlinModule())
        return fileStream.use { stream ->
            mapper.readValue(stream)
        }
    }

    /**
     * instead of reading the resource as it is done in the interface we will read a file by the absolute path here
     * if the path is provided, else will read the hardcoded file 'diktat-analysis.yml' from the package
     *
     * @param resourceFileName name of the resource which will be loaded using [classLoader]
     * @return [BufferedReader] representing loaded resource
     */
    override fun getConfigFile(resourceFileName: String): BufferedReader? {
        val resourceFile = File(resourceFileName)
        return if (resourceFile.exists()) {
            log.debug("Using diktat-analysis.yml file from the following path: ${resourceFile.absolutePath}")
            File(resourceFileName).bufferedReader()
        } else {
            log.debug("Using the default diktat-analysis.yml file from the class path")
            classLoader.getResourceAsStream(resourceFileName)?.bufferedReader()
        }
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(RulesConfigReader::class.java)
    }
}

open class TestAnchorsConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
    val testAnchors = config.getOrDefault("testDirs", "test").split(',')
}

// ================== utils for List<RulesConfig> from yml config

/**
 * Get [RulesConfig] for particular [Rule] object.
 *
 * @param rule a [Rule] which configuration will be returned
 * @return [RulesConfig] for a particular rule if it is found, else null
 */
fun List<RulesConfig>.getRuleConfig(rule: Rule): RulesConfig? = this.find { it.name == rule.ruleName() }

/**
 * Get [RulesConfig] representing common configuration part that can be used in any rule
 */
fun List<RulesConfig>.getCommonConfig() = find { it.name == DIKTAT_COMMON }

/**
 * checking if in yml config particular rule is enabled or disabled
 * (!) the default value is "true" (in case there is no config specified)
 *
 * @param rule a [Rule] which is being checked
 * @return true if rule is enabled in configuration, else false
 */
fun List<RulesConfig>.isRuleEnabled(rule: Rule): Boolean {
    val ruleMatched = getRuleConfig(rule)
    return ruleMatched?.enabled ?: true
}
