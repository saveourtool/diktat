package org.cqfn.diktat.ktlint

import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.Reporter
import java.util.concurrent.atomic.AtomicInteger

/**
 * An implementation of [Reporter] which counts [LintError]s
 */
class LintErrorReporter : Reporter {
    private val errorCounter: AtomicInteger = AtomicInteger()

    override fun onLintError(file: String, err: LintError, corrected: Boolean) {
        errorCounter.incrementAndGet()
    }

    /**
     * @return true if there are no reported [LintError], otherwise -- false.
     */
    fun isEmpty(): Boolean = errorCounter.get() == 0

    /**
     * @return count of reported [LintError]
     */
    fun errorCount(): Int = errorCounter.get()
}
