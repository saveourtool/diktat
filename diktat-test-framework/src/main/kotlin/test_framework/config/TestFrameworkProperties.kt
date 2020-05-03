package test_framework.config

import common.ApplicationProperties

class TestFrameworkProperties(propertiesFileName: String?) : ApplicationProperties(propertiesFileName!!) {
    private val testFrameworkResourcePath: String
        get() = properties.getProperty("test.framework.dir")

    private val testFilesDir: String
        get() = properties.getProperty("test.files.dir")

    val testFrameworkArgsRelativePath: String
        get() = testFrameworkResourcePath + "/" + properties.getProperty("test.framework.arguments")

    val testFilesRelativePath: String
        get() = "$testFrameworkResourcePath/$testFilesDir"

    val testConfigsRelativePath: String
        get() = testFrameworkResourcePath + "/" + properties.getProperty("test.configs.dir")

    val isParallelMode: Boolean
        get() = java.lang.Boolean.getBoolean(properties.getProperty("parallel.mode"))
}
