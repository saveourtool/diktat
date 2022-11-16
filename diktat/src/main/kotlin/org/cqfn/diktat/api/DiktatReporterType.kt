package org.cqfn.diktat.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Type of reporting in `diktat`
 */
@Serializable
enum class DiktatReporterType {
    @SerialName("plain")
    PLAIN,
    @SerialName("plain_group_by_file")
    PLAIN_GROUP_BY_FILE,
    @SerialName("json")
    JSON,
    @SerialName("sarif")
    SARIF,
    @SerialName("checkstyle")
    CHECKSTYLE,
    @SerialName("html")
    HTML,
}
