package org.cqfn.diktat

import org.cqfn.diktat.api.DiktatCallback
import java.nio.file.Path

/**
 * Processor to run `diktat`
 */
abstract class DiktatProcessor {
    /**
     * Run `diktat fix` on provided [file] using [callback] for detected errors and returned formatted file content.
     *
     * @param file
     * @param callback
     * @return result of `diktat fix`
     */
    abstract fun fix(file: Path, callback: DiktatCallback): String

    /**
     * Run `diktat check` on provided [file] using [callback] for detected errors.
     *
     * @param file
     * @param callback
     */
    abstract fun check(file: Path, callback: DiktatCallback)
}
