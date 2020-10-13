package org.cqfn.diktat.test.framework.config

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.BufferedReader
import java.io.IOException
import java.util.stream.Collectors
import org.cqfn.diktat.common.config.reader.JsonResourceConfigReader

/**
 * A [JsonResourceConfigReader] to read tests configuration as [TestConfig]
 */
class TestConfigReader(configFilePath: String, override val classLoader: ClassLoader) : JsonResourceConfigReader<TestConfig?>() {
    /**
     * The [TestConfig] which is read from
     */
    val config: TestConfig? = readResource(configFilePath)

    /**
     * @param fileStream input stream of data from config file
     * @return [TestConfig] read from file
     */
    @Throws(IOException::class)
    override fun parseResource(fileStream: BufferedReader): TestConfig {
        val jsonValue = fileStream.lines().collect(Collectors.joining())
        return ObjectMapper().readValue(jsonValue, TestConfig::class.java)
    }
}
