package com.saveourtool.diktat.api

/**
 * @property id
 * @property extension
 */
enum class DiktatReporterType(
    val id: String,
    val extension: String,
) {
    PLAIN("plain", "txt"),
    SARIF("sarif", "sarif"),
    JSON("json", "json"),
    CHECKSTYLE("checkstyle", "xml"),
    HTML("html", "html"),
    NONE("none", ""),
}
