package org.cqfn.diktat.cli

data class DiktatProperties(
    val config: String,
    val reporter: String,
    val output: String,
    val logLevel: DiktatLogLevel,
)
