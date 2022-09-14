package com.saveourtool.save.reporter.json

/**
 * Reporter that produces a JSON report as a [Report]
 *
 * @property out a sink for output
 *
 * @param builder additional configuration lambda for serializers module
 */
class JsonReporter(
    override val out: BufferedSink,
    builder: PolymorphicModuleBuilder<Plugin.TestFiles>.() -> Unit = {}
) : Reporter
