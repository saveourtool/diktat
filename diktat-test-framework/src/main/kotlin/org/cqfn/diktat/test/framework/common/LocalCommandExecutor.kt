package org.cqfn.diktat.test.framework.common

import mu.KotlinLogging
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking

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
            return runBlocking(Dispatchers.IO) {
                ExecutionResult(
                    process.inputStream.readLinesAsync("OUTPUT").toList(),
                    process.errorStream.readLinesAsync("ERROR").toList(),
                )
            }
        } catch (ex: IOException) {
            log.error("Execution of $command failed", ex)
        }
        return ExecutionResult(emptyList(), emptyList())
    }

    companion object {
        private val log = KotlinLogging.logger {}

        fun InputStream.readLinesAsync(streamType: String): Flow<String> = flow {
                try {
                    val bufferedReader = this@readLinesAsync.bufferedReader(Charset.defaultCharset())
                    while (true) {
                        val line = bufferedReader.readLine() ?: break
                        emit(line)
                    }
                } catch (ex: IOException) {
                    log.error(ex) { "Failed to consume and display the input stream of type $streamType." }
                }
            }.flowOn(Dispatchers.IO)
    }
}
