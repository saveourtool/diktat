package com.saveourtool.save.reporter.json

/**
 * Reporter that produces a JSON report as a [Report]
 *
 * @param builder additional configuration lambda for serializers module
 * @property out a sink for output
 */
class JsonReporter(
    override val out: BufferedSink,
    builder: PolymorphicModuleBuilder<Plugin.TestFiles>.() -> Unit = {}
) : Reporter
