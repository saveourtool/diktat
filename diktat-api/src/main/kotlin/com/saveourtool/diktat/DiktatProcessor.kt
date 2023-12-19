package com.saveourtool.diktat

import com.saveourtool.diktat.api.DiktatCallback
import java.nio.file.Path

/**
 * Processor to run `diktat`
 */
interface DiktatProcessor {
    /**
     * Run `diktat fix` on provided [file] using [callback] for detected errors and returned formatted file content.
     *
     * @param file
     * @param callback
     * @return result of `diktat fix`
     */
    fun fix(file: Path, callback: DiktatCallback): String

    /**
     * Run `diktat fix` on provided [code] using [callback] for detected errors and returned formatted code.
     *
     * @param code
     * @param virtualPath a path which should be taken into account in processing of [code]
     * @param callback
     * @return result of `diktat fix`
     */
    fun fix(
        code: String,
        virtualPath: Path?,
        callback: DiktatCallback,
    ): String

    /**
     * Run `diktat check` on provided [file] using [callback] for detected errors.
     *
     * @param file
     * @param callback
     */
    fun check(file: Path, callback: DiktatCallback)

    /**
     * Run `diktat check` on provided [code] using [callback] for detected errors.
     *
     * @param code
     * @param virtualPath a path which should be taken into account in processing of [code]
     * @param callback
     */
    fun check(
        code: String,
        virtualPath: Path?,
        callback: DiktatCallback,
    )
}
