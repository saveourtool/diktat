package org.cqfn.diktat.test.framework.config

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
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
        val jsonValue: String = fileStream.lines().collect(Collectors.joining())
        return Json.decodeFromString<TestConfig>(jsonValue)
    }
}
