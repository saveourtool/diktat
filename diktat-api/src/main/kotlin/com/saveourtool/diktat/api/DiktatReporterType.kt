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
    PLAIN("plain", "txt"),
    PLAIN_GROUP_BY_FILE("plain-group-by-file", "txt"),
    SARIF("sarif", "sarif"),
    ;
}
