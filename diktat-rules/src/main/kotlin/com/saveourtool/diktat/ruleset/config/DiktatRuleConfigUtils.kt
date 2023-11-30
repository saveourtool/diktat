/**
 * Util methods for List<RulesConfig>
 */

package com.saveourtool.diktat.ruleset.config

import com.saveourtool.diktat.api.DiktatRuleConfig
import com.saveourtool.diktat.api.DiktatRuleNameAware
import com.saveourtool.diktat.common.config.rules.RulesConfig

/**
 * Name of common configuration
 */
const val DIKTAT_COMMON = "DIKTAT_COMMON"


/**
 * Parse string into KotlinVersion
 *
 * @return KotlinVersion from configuration
 */
internal fun String.kotlinVersion(): KotlinVersion {
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

