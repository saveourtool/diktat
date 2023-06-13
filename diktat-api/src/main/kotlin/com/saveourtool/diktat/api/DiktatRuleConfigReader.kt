package com.saveourtool.diktat.api

import java.io.InputStream

/**
 * A reader for [DiktatRuleConfig]
 */
fun interface DiktatRuleConfigReader : Function1<InputStream, List<DiktatRuleConfig>> {
    /**
     * @param inputStream
     * @return parsed [DiktatRuleConfig]s
     */
    override operator fun invoke(inputStream: InputStream): List<DiktatRuleConfig>
}
