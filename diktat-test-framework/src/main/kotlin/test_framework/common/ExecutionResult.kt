package test_framework.common

/**
 * Class that keeps the result of executed command
 * @param stdOut standard output
 * @param stdErr error stream
 */
data class ExecutionResult (val stdOut: List<String>, val stdErr: List<String>)


