package com.saveourtool.diktat

    override fun boo() {
     val listTestResult: MutableList<TestResult> = mutableListOf()
    files.chunked(warnPluginConfig.batchSize ?: 1).map { chunk ->
        handleTestFile(chunk.map { it.single() }, warnPluginConfig, generalConfig)
    }.forEach { listTestResult.addAll(it) }
}