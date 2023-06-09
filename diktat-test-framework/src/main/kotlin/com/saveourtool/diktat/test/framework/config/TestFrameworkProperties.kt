package com.saveourtool.diktat.test.framework.config

import com.saveourtool.diktat.common.config.reader.ApplicationProperties

/**
 * [ApplicationProperties] for running tests
 */
class TestFrameworkProperties(propertiesFileName: String) : ApplicationProperties(propertiesFileName) {
    private val testFrameworkResourcePath: String by lazy { properties.getProperty("test.framework.dir") }
    private val testFilesDir: String by lazy { properties.getProperty("test.files.dir") }

    /**
     * Relative path to a file with arguments for tests runner
     */
    val testFrameworkArgsRelativePath: String by lazy { testFrameworkResourcePath + "/" + properties.getProperty("test.framework.arguments") }

    /**
     * Relative path to test files directory
     */
    val testFilesRelativePath: String = "$testFrameworkResourcePath/$testFilesDir"

    /**
     * Relative path to test configs directory
     */
    val testConfigsRelativePath: String by lazy { testFrameworkResourcePath + "/" + properties.getProperty("test.configs.dir") }

    /**
     * Whether tests should be run in parallel
     */
    val isParallelMode: Boolean by lazy { java.lang.Boolean.getBoolean(properties.getProperty("parallel.mode")) }
}
