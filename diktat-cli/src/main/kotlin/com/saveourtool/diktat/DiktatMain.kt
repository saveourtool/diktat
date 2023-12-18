/**
 * The file contains main method
 */

package com.saveourtool.diktat

import com.saveourtool.diktat.api.DiktatProcessorListener
import com.saveourtool.diktat.cli.DiktatMode
import com.saveourtool.diktat.cli.DiktatProperties

import io.github.oshai.kotlinlogging.KotlinLogging

import java.nio.file.Path
import java.nio.file.Paths

import kotlin.io.path.absolutePathString
import kotlin.system.exitProcess

private val log = KotlinLogging.logger { }

private val loggingListener = object : DiktatProcessorListener {
    override fun before(file: Path) {
        log.debug {
            "Start processing the file: $file"
        }
    }
}

fun main(args: Array<String>) {
    val properties = DiktatProperties.parse(diktatReporterFactory, args)
    properties.configureLogger()

    log.debug {
        "Loading diktatRuleSet using config ${properties.config}"
    }
    val currentFolder = Paths.get(".").toAbsolutePath().normalize()
    val diktatRunnerArguments = properties.toRunnerArguments(
        sourceRootDir = currentFolder,
        loggingListener = loggingListener,
    )

    val diktatRunner = diktatRunnerFactory(diktatRunnerArguments)
    val unfixedErrors = when (properties.mode) {
        DiktatMode.CHECK -> diktatRunner.checkAll(diktatRunnerArguments)
        DiktatMode.FIX -> diktatRunner.fixAll(diktatRunnerArguments) { updatedFile ->
            log.warn {
                "Original and formatted content differ, writing to ${updatedFile.absolutePathString()}..."
            }
        }
    }
    if (unfixedErrors > 0) {
        exitProcess(1)
    }
}
