package test_framework.config

import com.fasterxml.jackson.databind.ObjectMapper
import common.JsonResourceConfigReader
import java.io.File
import java.io.IOException

class TestConfigReader(configFilePath: String?) : JsonResourceConfigReader<TestConfig?> {
    val config: TestConfig? = readResource(configFilePath!!)

    @Throws(IOException::class)
    override fun parseResource(file: File): TestConfig {
        return ObjectMapper().readValue(file, TestConfig::class.java)
    }

}