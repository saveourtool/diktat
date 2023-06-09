package org.cqfn.diktat.test.framework.config

import org.cqfn.diktat.common.config.reader.JsonResourceConfigReader

import java.io.BufferedReader
import java.io.IOException
import java.util.stream.Collectors

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * A [JsonResourceConfigReader] to read tests configuration as [TestConfig]
 * @property classLoader a [ClassLoader] to load configutation file
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
    @OptIn(ExperimentalSerializationApi::class)
    @Throws(IOException::class)
    override fun parseResource(fileStream: BufferedReader): TestConfig {
        val jsonValue: String = fileStream.lines().collect(Collectors.joining())
        return Json.decodeFromString(jsonValue)
    }
}
