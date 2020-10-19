package org.cqfn.diktat.test.framework.common

/**
 * Class that keeps the result of executed command
 *
 * @param stdOut standard output
 * @param stdErr error stream
 * @property stdOut content from stdout stream
 * @property stdErr content from stderr stream
 */
data class ExecutionResult(val stdOut: List<String>, val stdErr: List<String>)
