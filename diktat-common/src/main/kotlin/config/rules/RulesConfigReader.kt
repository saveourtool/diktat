package config.rules

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import config.reader.JsonResourceConfigReader
import java.io.File

interface Rule {
    fun name (): String
}

data class RulesConfig(
    var name: String,
    var enabled: Boolean,
    var help: String,
    var warningText: String,
    var configuration: String
)

class RulesConfigReader : JsonResourceConfigReader<List<RulesConfig>> {
    override fun parseResource(file: File): List<RulesConfig> {
        val mapper = jacksonObjectMapper()
        return mapper.readValue(file)
    }
}

fun List<RulesConfig>.getRuleConfig(rule: Rule): RulesConfig? {
    return this.find { it.name == rule.name()}
}
