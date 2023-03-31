package org.cqfn.diktat.api

import java.io.OutputStream
import java.nio.file.Path

typealias DiktatReporter = DiktatProcessorListener

/**
 * A factory to create [DiktatReporter]
 */
fun interface DiktatReporterFactory : Function4<String, OutputStream, Map<String, String>, Path, DiktatReporter> {
    /**
     * @param id ID of [DiktatReporter]
     * @param outputStream
     * @param extraProperties extra properties which can be required to create [DiktatReporter]
     * @param sourceRootDir a dir to detect relative path for processing files
     * @return created [DiktatReporter]
     */
    override operator fun invoke(
        id: String,
        outputStream: OutputStream,
        extraProperties: Map<String, String>,
        sourceRootDir: Path,
    ): DiktatReporter
}
