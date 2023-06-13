// ;warn:$line:1: [HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE] files that contain multiple or no classes should contain description of what is inside of this file: there are 0 declared classes and/or objects (cannot be auto-corrected) (diktat-ruleset:header-comment)
package com.saveourtool.diktat

// ;warn:$line:1: [MISSING_KDOC_TOP_LEVEL] all public and internal top-level classes and functions should have Kdoc: boo (cannot be auto-corrected) (diktat-ruleset:kdoc-comments)
override fun boo() {
    val listTestResult: MutableList<TestResult> = mutableListOf()
    files.chunked(warnPluginConfig.batchSize ?: 1).map { chunk ->
        handleTestFile(chunk.map { it.single() }, warnPluginConfig, generalConfig)
    }.forEach { listTestResult.addAll(it) }
}
