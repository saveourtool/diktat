package org.cqfn.diktat.test.framework.common

import org.cqfn.diktat.common.utils.loggerWithKtlintConfig
import mu.KotlinLogging

import java.io.IOException

/**
 * Class that wraps shell [command] and can execute it
 */
class LocalCommandExecutor internal constructor(private val command: String) {
    /**
     * Execute [command]
     *
     * @return [ExecutionResult] of command execution
     */
    fun executeCommand(): ExecutionResult {
        try {
            log.info { "Executing command: $command" }
            val process = Runtime.getRuntime().exec(command)
            process.outputStream.close()
            val inputStream = process.inputStream
            val outputGobbler = StreamGobbler(inputStream, "OUTPUT") { msg, ex ->
                log.error(ex, msg)
            }
            outputGobbler.start()
            val errorStream = process.errorStream
            val errorGobbler = StreamGobbler(errorStream, "ERROR") { msg, ex ->
                log.error(ex, msg)
            }
            errorGobbler.start()
            return ExecutionResult(outputGobbler.content, errorGobbler.content)
        } catch (ex: IOException) {
            log.error("Execution of $command failed", ex)
        }
        return ExecutionResult(emptyList(), emptyList())
    }

    companion object {
        @Suppress("EMPTY_BLOCK_STRUCTURE_ERROR")
        private val log = KotlinLogging.loggerWithKtlintConfig {}
    }
}
