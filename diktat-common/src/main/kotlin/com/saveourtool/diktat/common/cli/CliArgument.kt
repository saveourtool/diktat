package com.saveourtool.diktat.common.cli

import org.apache.commons.cli.Option

import kotlinx.serialization.Serializable

/**
 * This class is used to serialize/deserialize json representation
 * that is used to store command line arguments
 *
 * @param shortName short argument representation like -h
 * @param helpDescr
 * @param longName long argument representation like --help
 * @param hasArgs indicates if option should have explicit argument
 * @param isRequired
 */
@Serializable
data class CliArgument(
    private val shortName: String,
    private val helpDescr: String,
    private val longName: String,
    private val hasArgs: Boolean,
    private val isRequired: Boolean
) {
    /**
     * Converts parameters received from json to [Option]
     *
     * @return an [Option]
     */
    fun convertToOption() = Option(shortName, longName, hasArgs, helpDescr).apply {
        isRequired = this@CliArgument.isRequired
    }
}
