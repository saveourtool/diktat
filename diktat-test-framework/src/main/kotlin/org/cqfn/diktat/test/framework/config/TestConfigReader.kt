package org.cqfn.diktat.test.framework.config

import org.cqfn.diktat.common.config.reader.JsonResourceConfigReader

import java.io.IOException
import java.io.InputStream

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

/**
 * A [JsonResourceConfigReader] to read tests configuration as [TestConfig]
 * @property classLoader a [ClassLoader] to load configuration file
 */
class TestConfigReader(configFilePath: String, classLoader: ClassLoader) : JsonResourceConfigReader<TestConfig>(classLoader) {
    /**
     * The [TestConfig] which is read from
     */
    val config: TestConfig? = readResource(configFilePath)

    /**
     * @param inputStream input stream of data from config file
     * @return [TestConfig] read from file
     */
    @OptIn(ExperimentalSerializationApi::class)
    @Throws(IOException::class)
    override fun parse(inputStream: InputStream): TestConfig = Json.decodeFromStream(inputStream)
}
