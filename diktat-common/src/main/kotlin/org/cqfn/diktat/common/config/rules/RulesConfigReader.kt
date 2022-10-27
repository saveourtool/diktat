/**
 * Classes and extensions needed to read and parse rules configuration file
 */

package org.cqfn.diktat.common.config.rules

import org.cqfn.diktat.common.config.reader.JsonResourceConfigReader
import org.cqfn.diktat.common.config.rules.RulesConfigReader.Companion.log
import org.cqfn.diktat.common.utils.loggerWithKtlintConfig

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import mu.KotlinLogging
import org.slf4j.Logger

import java.io.BufferedReader
import java.io.File
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

/**
 * Name of common configuration
 */
const val DIKTAT_COMMON = "DIKTAT_COMMON"

/**
 * Common application name, that is used in plugins and can be used to Suppress all diktat inspections on the
 * particular code block with @Suppress("diktat")
 */
const val DIKTAT = "diktat"

/**
 * this constant will be used everywhere in the code to mark usage of Diktat ruleset
 */
const val DIKTAT_RULE_SET_ID = "diktat-ruleset"
const val DIKTAT_ANALYSIS_CONF = "diktat-analysis.yml"
const val DIKTAT_CONF_PROPERTY = "diktat.config.path"

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
 * @property ignoreAnnotated if a code block is marked with this annotations - it will not be checked by this rule
 */
@Serializable
data class RulesConfig(
    val name: String,
    val enabled: Boolean = true,
    val configuration: Map<String, String> = emptyMap(),
    val ignoreAnnotated: Set<String> = emptySet(),
)

/**
 * Configuration that allows customizing additional options of particular rules.
 * @property config a map of strings with configuration options for a particular rule
 */
open class RuleConfiguration(protected val config: Map<String, String>)

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
    @OptIn(ExperimentalSerializationApi::class)
    override fun parseResource(fileStream: BufferedReader): List<RulesConfig> = fileStream.use { stream ->
        yamlSerializer.decodeFromString<List<RulesConfig>>(stream.readLines().joinToString(separator = "\n")).reversed().distinctBy { it.name }
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
        /**
         * A [Logger] that can be used
         */
        val log: Logger = KotlinLogging.loggerWithKtlintConfig(RulesConfigReader::class)
    }
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
        val testDirs = (configuration ?: emptyMap()).getOrDefault("testDirs", "test").split(',').map { it.trim() }
        if (testDirs.any { !it.lowercase(Locale.getDefault()).endsWith("test") }) {
            log.error("test directory names should end with `test`")
        }
        testDirs
    }

    /**
     * Start of package name, which shoould be common, e.g. org.example.myproject
     */
    val domainName: String? by lazy {
        configuration?.get("domainName")
    }

    /**
     * Get disable chapters from configuration
     */
    val disabledChapters: String? by lazy {
        configuration?.get("disabledChapters")
    }

    /**
     * Get version of kotlin from configuration
     */
    val kotlinVersion: KotlinVersion by lazy {
        configuration?.get("kotlinVersion")?.kotlinVersion() ?: run {
            if (visitorCounter.incrementAndGet() == 1) {
                log.error("Kotlin version not specified in the configuration file. Will be using ${KotlinVersion.CURRENT} version")
            }
            KotlinVersion.CURRENT
        }
    }

    /**
     * Get source directories from configuration
     */
    val srcDirectories: List<String> by lazy {
        val srcDirs = configuration?.get("srcDirectories")?.split(",")?.map { it.trim() } ?: listOf("main")
        if (srcDirs.any { !it.lowercase(Locale.getDefault()).endsWith("main") }) {
            log.error("source directory names should end with `main`")
        }
        srcDirs
    }

    companion object {
        /**
         * Counter that helps not to raise multiple warnings about kotlin version
         */
        var visitorCounter = AtomicInteger(0)
    }
}

// ================== utils for List<RulesConfig> from yml config

/**
 * @return common configuration from list of all rules configuration
 */
fun List<RulesConfig>.getCommonConfiguration() = CommonConfiguration(getCommonConfig()?.configuration)

/**
 * Get [RulesConfig] for particular [Rule] object.
 *
 * @param rule a [Rule] which configuration will be returned
 * @return [RulesConfig] for a particular rule if it is found, else null
 */
fun List<RulesConfig>.getRuleConfig(rule: Rule): RulesConfig? = this.find { it.name == rule.ruleName() }

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

/**
 * @param rule diktat inspection
 * @param annotations set of annotations that are annotating a block of code
 * @return true if the code block is marked with annotation that is in `ignored list` in the rule
 */
fun List<RulesConfig>.isAnnotatedWithIgnoredAnnotation(rule: Rule, annotations: Set<String>): Boolean =
    getRuleConfig(rule)
        ?.ignoreAnnotated
        ?.map { it.trim() }
        ?.map { it.trim('"') }
        ?.intersect(annotations)
        ?.isNotEmpty()
        ?: false

/**
 * Parse string into KotlinVersion
 *
 * @return KotlinVersion from configuration
 */
fun String.kotlinVersion(): KotlinVersion {
    require(this.contains("^(\\d+\\.)(\\d+)\\.?(\\d+)?$".toRegex())) {
        "Kotlin version format is incorrect"
    }
    val versions = this.split(".").map { it.toInt() }
    return if (versions.size == 2) {
        KotlinVersion(versions[0], versions[1])
    } else {
        KotlinVersion(versions[0], versions[1], versions[2])
    }
}

/**
 * Get [RulesConfig] representing common configuration part that can be used in any rule
 */
private fun List<RulesConfig>.getCommonConfig() = find { it.name == DIKTAT_COMMON }
