package org.cqfn.diktat.test.framework.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.cqfn.diktat.common.config.reader.JsonResourceConfigReader
import org.cqfn.diktat.common.config.rules.RulesConfig
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.util.stream.Collectors

class TestConfigReader(configFilePath: String?, override val classLoader: ClassLoader) : JsonResourceConfigReader<TestConfig?>() {
    val config: TestConfig? = readResource(configFilePath!!)

    @Throws(IOException::class)
    override fun parseResource(fileStream: BufferedReader): TestConfig {
        val jsonValue = fileStream.lines().collect(Collectors.joining())
        return ObjectMapper().readValue(jsonValue, TestConfig::class.java)
    }
}
