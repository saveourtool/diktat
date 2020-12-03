/**
 * Classes and extensions needed to read and parse rules configuration file
 */

package org.cqfn.diktat.common.config.rules

import org.cqfn.diktat.common.config.reader.JsonResourceConfigReader

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.io.BufferedReader
import java.io.File

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

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
 * @property name name of the rule
 * @property enabled
 * @property configuration a map of strings with configuration options
 */
@Serializable
data class RulesConfig(
    val name: String,
    val enabled: Boolean = true,
    val configuration: Map<String, String> = emptyMap()
)

/**
 * Configuration that allows customizing additional options of particular rules.
 * @property config a map of strings with configuration options for a particular rule
 */
open class RuleConfiguration(protected val config: Map<String, String>)
object EmptyConfiguration : RuleConfiguration(mapOf())

/**
 * class returns the list of configurations that we have read from a yml: diktat-analysis.yml
 * @property classLoader a [ClassLoader] used to load configuration file
 */
open class RulesConfigReader(override val classLoader: ClassLoader) : JsonResourceConfigReader<List<RulesConfig>>() {
    private val yamlSerializer by lazy { Yaml(configuration = YamlConfiguration(strictMode = true)) }

    /**
     * Parse resource file into list of [RulesConfig]
     *
     * @param fileStream a [BufferedReader] representing loaded rules config file
     * @return list of [RulesConfig]
     */
    override fun parseResource(fileStream: BufferedReader): List<RulesConfig> = fileStream.use { stream ->
        yamlSerializer.decodeFromString(stream.readLines().joinToString(separator = "\n"))
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

/**
 * @return common configuration from list of all rules configuration
 */
fun List<RulesConfig>.getCommonConfiguration() = lazy {
    CommonConfiguration(getCommonConfig()?.configuration)
}

/**
 * class returns the list of common configurations that we have read from a configuration map
 *
 * @param configuration map of common configuration
 */
data class CommonConfiguration(private val configuration: Map<String, String>?) {
    /**
     * List of directory names which will be used to detect test sources
     */
    val testAnchors: List<String> by lazy {
        (configuration ?: mapOf()).getOrDefault("testDirs", "test").split(',')
    }

    /**
     * Start of package name, which shoould be common, e.g. org.example.myproject
     */
    val domainName: String by lazy {
        (configuration ?: mapOf()).getOrDefault("domainName", "")
    }

    /**
     * False if configuration has been read from config file, true if defaults are used
     */
    val isDefault = configuration == null
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
private fun List<RulesConfig>.getCommonConfig() = find { it.name == DIKTAT_COMMON }

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
