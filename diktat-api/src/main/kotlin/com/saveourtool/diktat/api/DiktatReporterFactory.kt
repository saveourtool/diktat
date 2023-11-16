package com.saveourtool.diktat.api

typealias DiktatReporter = DiktatProcessorListener

/**
 * A factory to create [DiktatReporter]
 */
interface DiktatReporterFactory : Function1<DiktatReporterCreationArguments, DiktatReporter> {
    /**
     * Set of supported IDs, must contain [DiktatReporterFactory.NONE_ID]
     */
    val ids: Set<String>

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

    companion object {
        /**
         * ID of [DiktatReporter] for disabled reporter
         */
        const val NONE_ID: String = "none"

        /**
         * ID of [DiktatReporter] for plain output
         */
        const val PLAIN_ID: String = "plain"
    }
}
