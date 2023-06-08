package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.api.DiktatRuleConfig
import org.cqfn.diktat.api.DiktatRuleConfigReader
import org.cqfn.diktat.common.config.rules.DIKTAT_COMMON
import org.cqfn.diktat.common.config.rules.RulesConfigReader
import org.cqfn.diktat.ruleset.constants.Warnings
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

    private fun validate(config: org.cqfn.diktat.common.config.rules.RulesConfig) =
        require(config.name == DIKTAT_COMMON || config.name in Warnings.names) {
            val closestMatch = Warnings.names.minByOrNull { Levenshtein.distance(it, config.name) }
            "Warning name <${config.name}> in configuration file is invalid, did you mean <$closestMatch>?"
        }
}
