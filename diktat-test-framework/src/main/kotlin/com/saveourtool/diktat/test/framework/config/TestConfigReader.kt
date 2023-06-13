package com.saveourtool.diktat.test.framework.config

import com.saveourtool.diktat.common.config.reader.AbstractConfigReader

import java.io.IOException
import java.io.InputStream

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

/**
 * A [AbstractConfigReader] to read tests configuration as [TestConfig]
 */
class TestConfigReader(configFilePath: String, classLoader: ClassLoader) : AbstractConfigReader<TestConfig>() {
    /**
     * The [TestConfig] which is read from
     */
    val config: TestConfig? = classLoader.getResourceAsStream(configFilePath)?.let { read(it) }

    /**
     * @param inputStream input stream of data from config file
     * @return [TestConfig] read from file
     */
    @OptIn(ExperimentalSerializationApi::class)
    @Throws(IOException::class)
    override fun parse(inputStream: InputStream): TestConfig = Json.decodeFromStream(inputStream)
}
