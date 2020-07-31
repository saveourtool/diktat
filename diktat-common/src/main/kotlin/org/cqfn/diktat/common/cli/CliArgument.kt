package org.cqfn.diktat.common.cli

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.apache.commons.cli.Option

/**
 * This class is used to serialize/deserialize json representation
 * that is used to store command line arguments
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class CliArgument @JsonCreator internal constructor(
    // short argument representation like -h
    @param:JsonProperty("shortName") private val shortName: String,
    @param:JsonProperty("helpDescr") private val helpDescr: String,
    // long argument representation like --help
    @param:JsonProperty("longName") private val longName: String,
    // indicates if option should have explicit argument
    @param:JsonProperty("hasArgs") private val hasArgs: Boolean,
    @param:JsonProperty("isRequired") private val isRequired: Boolean) {

    override fun toString(): String {
        return "(shortName: " + shortName + ", helpDescr: " + helpDescr + ", longName: " +
            longName + ", hasArgs: " + hasArgs + ", isRequired: " + isRequired + ")"
    }

    /**
     * Converts parameters received from json to [Option]
     * @return an [Option]
     */
    fun convertToOption(): Option {
        val resOption = Option(shortName, longName, hasArgs, helpDescr)
        resOption.isRequired = isRequired
        return resOption
    }
}
