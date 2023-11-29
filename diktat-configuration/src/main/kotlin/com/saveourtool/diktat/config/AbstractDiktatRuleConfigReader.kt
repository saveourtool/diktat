package com.saveourtool.diktat.config

import com.saveourtool.diktat.api.DiktatRuleConfig
import com.saveourtool.diktat.api.DiktatRuleConfigReader
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.IOException
import java.io.InputStream
import kotlin.jvm.Throws

/**
 * This class is used to read input stream in any format that you will specify.
 * Usage:
 * 1) implement this class with implementing the method:
 *    a. parse - implement parser for your file format (for example parse it to a proper json)
 * 2) Use your new class MyReader().read(someInputStream)
 */
abstract class AbstractDiktatRuleConfigReader : DiktatRuleConfigReader {
    /**
     * @param inputStream - input stream
     * @return list of [DiktatRuleConfig] if resource has been parsed successfully
     */
    override fun invoke(inputStream: InputStream): List<DiktatRuleConfig> = read(inputStream).orEmpty()

    private fun read(inputStream: InputStream): List<DiktatRuleConfig>? = try {
        parse(inputStream)
    } catch (e: IOException) {
        log.error(e) {
            "Cannot read config from input stream due to: "
        }
        null
    }

    /**
     * you can specify your own parser, in example for parsing stream as a json
     *
     * @param inputStream a [InputStream] representing loaded content
     * @return resource parsed as list of [DiktatRuleConfig]
     * @throws IOException
     */
    @Throws(IOException::class)
    protected abstract fun parse(inputStream: InputStream): List<DiktatRuleConfig>

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
