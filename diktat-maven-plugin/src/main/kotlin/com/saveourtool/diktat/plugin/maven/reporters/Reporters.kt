package com.saveourtool.diktat.plugin.maven.reporters

import org.apache.maven.plugins.annotations.Parameter

/**
 * Configuration for reporters
 */
class Reporters {
    /**
     * Configure *plain* reporter
     */
    @Parameter
    var plain: PlainReporter? = null

    /**
     * Configure *json* reporter
     */
    @Parameter
    var json: JsonReporter? = null

    /**
     * Configure *sarif* reporter
     */
    @Parameter
    var sarif: SarifReporter? = null

    /**
     * Configure *sarif* reporter for GitHub actions
     */
    @Parameter
    var gitHubActions: GitHubActionsReporter? = null

    /**
     * Configure *checkstyle* reporter
     */
    @Parameter
    var checkstyle: CheckstyleReporter? = null

    /**
     * Configure *html* reporter
     */
    @Parameter
    var html: HtmlReporter? = null

    /**
     * @return all configured reporters
     */
    fun getAll(): List<Reporter> = listOfNotNull(
        plain,
        json,
        sarif,
        gitHubActions,
        checkstyle,
        html,
    )
}
