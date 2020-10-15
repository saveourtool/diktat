package org.cqfn.diktat.common.cli

import org.apache.commons.cli.Option
import kotlinx.serialization.*

/**
 * This class is used to serialize/deserialize json representation
 * that is used to store command line arguments
 */
@Serializable
data class CliArgument (
        // short argument representation like -h
        private val shortName: String,
        private val helpDescr: String,
        // long argument representation like --help
        private val longName: String,
        // indicates if option should have explicit argument
        private val hasArgs: Boolean,
        private val isRequired: Boolean) {
    /**
     * Converts parameters received from json to [Option]
     *
     * @return an [Option]
     */
    fun convertToOption(): Option {
        val resOption = Option(shortName, longName, hasArgs, helpDescr)
        resOption.isRequired = isRequired
        return resOption
    }
}
