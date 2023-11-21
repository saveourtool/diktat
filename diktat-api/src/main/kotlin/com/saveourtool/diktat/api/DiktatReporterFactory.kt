package com.saveourtool.diktat.api

typealias DiktatReporter = DiktatProcessorListener

/**
 * A factory to create [DiktatReporter]
 */
interface DiktatReporterFactory : Function1<DiktatReporterCreationArguments, DiktatReporter> {
    /**
     * Names of color for plain output
     */
    val colorNamesInPlain: Set<String>

    /**
     * @param args
     * @return created [DiktatReporter]
     */
    override operator fun invoke(
        args: DiktatReporterCreationArguments,
    ): DiktatReporter
}
