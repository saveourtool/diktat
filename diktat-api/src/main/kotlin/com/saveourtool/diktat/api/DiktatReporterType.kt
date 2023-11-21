package com.saveourtool.diktat.api

/**
 * @property id
 * @property extension
 */
enum class DiktatReporterType(
    val id: String,
    val extension: String,
) {
    CHECKSTYLE("checkstyle", "xml"),
    HTML("html", "html"),
    JSON("json", "json"),
    NONE("none", ""),
    PLAIN("plain", "txt"),
    SARIF("sarif", "sarif"),
    ;
}
