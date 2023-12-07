package com.saveourtool.diktat.ruleset.config

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.decodeFromStream
import java.io.InputStream

/**
 * class returns the list of configurations that we have read from a yml: diktat-analysis.yml
 */
class DiktatRuleConfigYamlReader : AbstractDiktatRuleConfigReader() {
    private val yamlSerializer by lazy { Yaml(configuration = YamlConfiguration(strictMode = true)) }

    /**
     * Parse resource file into list of [RulesConfig]
     *
     * @param inputStream a [InputStream] representing loaded rules config file
     * @return list of [RulesConfig]
     */
    override fun parse(inputStream: InputStream): List<RulesConfig> = yamlSerializer.decodeFromStream<List<RulesConfig>>(inputStream)
}
