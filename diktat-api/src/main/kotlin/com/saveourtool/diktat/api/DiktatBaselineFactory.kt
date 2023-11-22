package com.saveourtool.diktat.api

import java.nio.file.Path

/**
 * A factory to load or generate [DiktatBaseline]
 */
interface DiktatBaselineFactory {
    /**
     * @param baselineFile
     * @param sourceRootDir a dir to detect relative path for processing files
     * @return Loaded [DiktatBaseline] from [baselineFile] or null if it gets an error in loading
     */
    fun tryToLoad(
        baselineFile: Path,
        sourceRootDir: Path?,
    ): DiktatBaseline?

    /**
     * @param baselineFile
     * @param sourceRootDir a dir to detect relative path for processing files
     * @return [DiktatProcessorListener] which generates baseline in [baselineFile]
     */
    fun generator(
        baselineFile: Path,
        sourceRootDir: Path?,
    ): DiktatProcessorListener
}
