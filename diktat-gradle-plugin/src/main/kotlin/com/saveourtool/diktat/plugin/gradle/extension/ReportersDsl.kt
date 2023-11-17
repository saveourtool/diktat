package com.saveourtool.diktat.plugin.gradle.extension

import com.saveourtool.diktat.plugin.gradle.defaultReportLocation
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * Configuration for reporters
 */
abstract class ReportersDsl @Inject constructor(
    private val project: Project,
    private val objectFactory: ObjectFactory,
    private val values: MutableList<Reporter>,
) {

    fun plain(action: Action<PlainReporter>): Unit = action.execute(newReporter("txt"))

    fun plain() {
        plain(emptyAction())
    }


    fun json(action: Action<JsonReporter>): Unit = action.execute(newReporter("json"))

    fun json() {
        json(emptyAction())
    }

    fun sarif(action: Action<SarifReporter>): Unit = action.execute(newReporter("json"))

    fun sarif() {
        sarif(emptyAction())
    }

    fun githubAction() {
        sarif(Action {
            it.output.set(project.defaultReportLocation(extension = "sarif"))
            it.mergeOutput.set(project.rootProject.defaultReportLocation(fileName = "diktat-merged", extension = "sarif"))
        })
    }

    fun checkstyle(action: Action<CheckstyleReporter>): Unit = action.execute(newReporter("xml"))

    fun checkstyle() {
        checkstyle(emptyAction())
    }

    fun html() {
        newReporter<HtmlReporter>("html")
    }

    fun html(action: Action<HtmlReporter>): Unit = action.execute(newReporter("html"))

    fun custom(action: Action<CustomReporter>): Unit = action.execute(newReporter())

    private inline fun <reified T : Reporter> newReporter(extension: String? = null): T = objectFactory.newInstance(T::class.java)
        .apply { values.add(this) }
        .also { reporter ->
            extension?.run {
                reporter.output.convention(project.defaultReportLocation(extension))
            }
        }

    private inline fun <reified T : Reporter> emptyAction() = Action<T> { }

    companion object {
        fun Reporter.getId(): String = when (this) {
            is JsonReporter -> "json"
            is SarifReporter -> "sarif"
            is CheckstyleReporter -> "checkstyle"
            is HtmlReporter -> "html"
            is PlainReporter -> "plain"
            is CustomReporter -> id.get()
            else -> error("Not supported reporter ${this.javaClass.name}")
        }
    }
}


abstract class JsonReporter: Reporter

abstract class SarifReporter: Reporter {
    /**
     * Location for merged output
     */
    abstract val mergeOutput: RegularFileProperty
}
abstract class CheckstyleReporter: Reporter
abstract class HtmlReporter: Reporter
abstract class PlainReporter: Reporter

abstract class CustomReporter : Reporter {
    /**
     * id of reporter for custom reporter
     */
    abstract val id: Property<String>

    /**
     * dependency for custom reporter
     */
    abstract val dependency: Property<String>
}

