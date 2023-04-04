package org.cqfn.diktat

import org.cqfn.diktat.api.DiktatCallback
import org.cqfn.diktat.api.DiktatProcessorListener
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
     * Run `diktat fix` for all [files] using [listener] during of processing and [formattedCodeHandler] to handle result of `diktat fix`.
     *
     * @param listener a listener which is called during processing.
     * @param files
     * @param formattedCodeHandler
     */
    fun fixAll(
        listener: DiktatProcessorListener = DiktatProcessorListener.empty,
        files: Collection<Path>,
        formattedCodeHandler: (Path, String) -> Unit,
    ) {
        listener.beforeAll(files)
        files.forEach { file ->
            listener.before(file)
            val formattedCode = fix(file) { error, isCorrected ->
                listener.onError(file, error, isCorrected)
            }
            formattedCodeHandler(file, formattedCode)
            listener.after(file)
        }
        listener.afterAll()
    }

    /**
     * Run `diktat check` on provided [file] using [callback] for detected errors.
     *
     * @param file
     * @param callback
     */
    abstract fun check(file: Path, callback: DiktatCallback)

    /**
     * Run `diktat check` for all [files] using [listener] during of processing.
     *
     * @param listener a listener which is called during processing.
     * @param files
     */
    fun checkAll(
        listener: DiktatProcessorListener = DiktatProcessorListener.empty,
        files: Collection<Path>,
    ) {
        listener.beforeAll(files)
        files.forEach { file ->
            listener.before(file)
            check(file) { error, isCorrected ->
                listener.onError(file, error, isCorrected)
            }
            listener.after(file)
        }
        listener.afterAll()
    }
}
