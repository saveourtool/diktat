package org.cqfn.diktat.cli

import org.cqfn.diktat.api.DiktatLogLevel

data class DiktatProperties(
    val config: String,
    val reporter: String,
    val output: String,
    val logLevel: DiktatLogLevel,
)
