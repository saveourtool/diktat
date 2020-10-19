package org.cqfn.diktat.test.framework.config

import org.cqfn.diktat.common.config.reader.ApplicationProperties

/**
 * [ApplicationProperties] for running tests
 */
class TestFrameworkProperties(propertiesFileName: String) : ApplicationProperties(propertiesFileName) {
    private val testFrameworkResourcePath: String
        get() = properties.getProperty("test.framework.dir")

    private val testFilesDir: String
        get() = properties.getProperty("test.files.dir")

    /**
     * Relative path to a file with arguments for tests runner
     */
    val testFrameworkArgsRelativePath: String
        get() = testFrameworkResourcePath + "/" + properties.getProperty("test.framework.arguments")

    /**
     * Relative path to test files directory
     */
    val testFilesRelativePath: String
        get() = "$testFrameworkResourcePath/$testFilesDir"

    /**
     * Relative path to test configs directory
     */
    val testConfigsRelativePath: String
        get() = testFrameworkResourcePath + "/" + properties.getProperty("test.configs.dir")

    /**
     * Whether tests should be run in parallel
     */
    val isParallelMode: Boolean
        get() = java.lang.Boolean.getBoolean(properties.getProperty("parallel.mode"))
}
