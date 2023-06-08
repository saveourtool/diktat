/**
 * Classes and extensions needed to read and parse rules configuration file
 */

package org.cqfn.diktat.common.config.rules

import org.cqfn.diktat.api.DiktatRuleConfig
import org.cqfn.diktat.common.config.reader.AbstractConfigReader
import org.cqfn.diktat.common.config.rules.RulesConfigReader.Companion.log

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.decodeFromStream
import mu.KLogger
import mu.KotlinLogging

import java.io.InputStream
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

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

typealias RulesConfig = DiktatRuleConfig

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
 * Configuration that allows customizing additional options of particular rules.
 * @property config a map of strings with configuration options for a particular rule
 */
open class RuleConfiguration(protected val config: Map<String, String>)

/**
 * class returns the list of configurations that we have read from a yml: diktat-analysis.yml
 */
open class RulesConfigReader : AbstractConfigReader<List<RulesConfig>>() {
    private val yamlSerializer by lazy { Yaml(configuration = YamlConfiguration(strictMode = true)) }

    /**
     * Parse resource file into list of [RulesConfig]
     *
     * @param inputStream a [InputStream] representing loaded rules config file
     * @return list of [RulesConfig]
     */
    override fun parse(inputStream: InputStream): List<RulesConfig> = yamlSerializer.decodeFromStream(inputStream)

    companion object {
        internal val log: KLogger = KotlinLogging.logger {}
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
        configuration?.get("srcDirectories")?.split(",")?.map { it.trim() } ?: listOf("main")
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
