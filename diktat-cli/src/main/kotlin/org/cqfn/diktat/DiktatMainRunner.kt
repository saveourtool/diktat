package org.cqfn.diktat

import org.cqfn.diktat.api.DiktatRuleSet
import org.cqfn.diktat.api.DiktatRuleSetFactory
import org.cqfn.diktat.ktlint.DiktatProcessorFactoryImpl
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetFactoryImpl
import mu.KotlinLogging
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.slf4j.event.Level

private val log = KotlinLogging.logger { }

fun main() {
    // a temporary
    val logLevel = Level.ERROR
    // set log level
    LogManager.getContext(false)
        .let { it as LoggerContext }
        .also { ctx ->
            ctx.configuration.rootLogger.level = when (logLevel) {
                Level.ERROR -> org.apache.logging.log4j.Level.ERROR
                Level.WARN -> org.apache.logging.log4j.Level.WARN
                Level.INFO -> org.apache.logging.log4j.Level.INFO
                Level.DEBUG -> org.apache.logging.log4j.Level.DEBUG
                Level.TRACE -> org.apache.logging.log4j.Level.TRACE
            }
        }
        .updateLoggers()

    // default implementations
    val diktatRuleSetFactory = DiktatRuleSetFactoryImpl()
    val diktatProcessorFactory = DiktatProcessorFactoryImpl()

    // ruleSet
    val diktatRuleSet: DiktatRuleSet = diktatRuleSetFactory()
    val diktatProcessor: DiktatProcessor = diktatProcessorFactory(diktatRuleSet)

    val code = mutableListOf<String>()
    while (true) {
        val line = readln()
        if (line == "CHECK") {
            diktatProcessor.check(
                code = code.joinToString(),
                isScript = true
            ) { error, _ ->
                println(error.toString())
            }
            code.clear()
        } else if (line == "FIX") {
            val result = diktatProcessor.fix(
                code = code.joinToString(),
                isScript = true
            ) { error, _ ->
                println(error.toString())
            }
            code.clear()
            println("Formatted code:")
            println(result)
        } else if (line == "END") {
            break
        } else {
            code.add(line)
        }
    }
}
