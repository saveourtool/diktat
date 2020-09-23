package org.cqfn.diktat.test.framework.common

import java.io.IOException
import org.slf4j.LoggerFactory

class LocalCommandExecutor internal constructor(private val command: String) {
    fun executeCommand(): ExecutionResult {
        try {
            log.info("Executing command: {}", command)
            val process = Runtime.getRuntime().exec(command)
            process.outputStream.close()
            val inputStream = process.inputStream
            val outputGobbler = StreamGobbler(inputStream, "OUTPUT")
            outputGobbler.start()
            val errorStream = process.errorStream
            val errorGobbler = StreamGobbler(errorStream, "ERROR")
            errorGobbler.start()
            return ExecutionResult(outputGobbler.content, errorGobbler.content)
        } catch (ex: IOException) {
            log.error("Execution of $command failed", ex)
        }
        return ExecutionResult(emptyList(), emptyList())
    }

    companion object {
        private val log = LoggerFactory.getLogger(LocalCommandExecutor::class.java)
    }
}
