package com.saveourtool.diktat.plugin.gradle.extension

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

/**
 * Configuration for reporters
 */
open class Reporters @Inject constructor(
    private val objectFactory: ObjectFactory,
) {
    /**
     * All reporters
     */
    val all: MutableList<Reporter> = mutableListOf()

    /**
     * Configure *plain* reporter with [action] configuration
     *
     * @param action
     */
    fun plain(action: Action<PlainReporter>): Unit = action.execute(newReporter())

    /**
     * Configure *plain* reporter with default configuration
     */
    fun plain() {
        plain(emptyAction())
    }

    /**
     * Configure *json* reporter with [action] configuration
     *
     * @param action
     */
    fun json(action: Action<JsonReporter>): Unit = action.execute(newReporter())

    /**
     * Configure *json* reporter with default configuration
     */
    fun json() {
        json(emptyAction())
    }

    /**
     * Configure *sarif* reporter with [action] configuration
     *
     * @param action
     */
    fun sarif(action: Action<SarifReporter>): Unit = action.execute(newReporter())

    /**
     * Configure *sarif* reporter with default configuration
     */
    fun sarif() {
        sarif(emptyAction())
    }

    /**
     * Configure *sarif* reporter for GitHub actions
     */
    fun gitHubActions() {
        newReporter<GitHubActionsReporter>()
    }

    /**
     * Configure *checkstyle* reporter with [action] configuration
     *
     * @param action
     */
    fun checkstyle(action: Action<CheckstyleReporter>): Unit = action.execute(newReporter())

    /**
     * Configure *checkstyle* reporter with default configuration
     */
    fun checkstyle() {
        checkstyle(emptyAction())
    }

    /**
     * Configure *html* reporter with default configuration
     */
    fun html() {
        html(emptyAction())
    }

    /**
     * Configure *html* reporter with [action] configuration
     *
     * @param action
     */
    fun html(action: Action<HtmlReporter>): Unit = action.execute(newReporter())

    private inline fun <reified T : Reporter> newReporter(): T = objectFactory.newInstance(T::class.java)
        .apply { all.add(this) }

    private inline fun <reified T : Reporter> emptyAction() = Action<T> { }
}
