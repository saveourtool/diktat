package com.saveourtool.diktat.ruleset.rules

import com.saveourtool.diktat.api.DiktatRuleConfig
import com.saveourtool.diktat.api.DiktatRuleConfigReader
import com.saveourtool.diktat.common.config.rules.DIKTAT_COMMON
import com.saveourtool.diktat.common.config.rules.RulesConfigReader
import com.saveourtool.diktat.ruleset.constants.Warnings
import org.jetbrains.kotlin.org.jline.utils.Levenshtein
import java.io.InputStream

/**
 * Default implementation for [DiktatRuleConfigReader]
 */
class DiktatRuleConfigReaderImpl : DiktatRuleConfigReader {
    private val yamlRuleConfigReader = RulesConfigReader()

    override fun invoke(inputStream: InputStream): List<DiktatRuleConfig> = yamlRuleConfigReader
        .read(inputStream)
        ?.onEach(::validate)
        ?: emptyList()

    private fun validate(config: com.saveourtool.diktat.common.config.rules.RulesConfig) =
        require(config.name == DIKTAT_COMMON || config.name in Warnings.names) {
            val closestMatch = Warnings.names.minByOrNull { Levenshtein.distance(it, config.name) }
            "Warning name <${config.name}> in configuration file is invalid, did you mean <$closestMatch>?"
        }
}
