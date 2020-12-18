package org.cqfn.diktat.plugin.gradle

import org.cqfn.diktat.plugin.gradle.DiktatGradlePlugin.Companion.DIKTAT_CHECK_TASK
import org.cqfn.diktat.plugin.gradle.DiktatGradlePlugin.Companion.DIKTAT_FIX_TASK

import generated.DIKTAT_VERSION
import generated.KTLINT_VERSION
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.VerificationTask

import java.io.File
import javax.inject.Inject

/**
 * A base diktat task for gradle <6.8, which wraps [JavaExec]
 */
open class DiktatJavaExecTaskBase @Inject constructor(
    gradleVersionString: String,
    diktatExtension: DiktatExtension,
    diktatConfiguration: Configuration,
    additionalFlags: Iterable<String> = emptyList()
) : JavaExec(), VerificationTask {
    /**
     * A backing [Property] for [getIgnoreFailures] and [setIgnoreFailures]
     */
    @get:Internal
    internal val ignoreFailuresProp: Property<Boolean> = project.objects.property(Boolean::class.javaObjectType)

    /**
     * Whether diktat should be executed via JavaExec or not.
     */
    @get:Internal
    internal var shouldRun = true

    init {
        group = "verification"
        if (isMainClassPropertySupported(gradleVersionString)) {
            // `main` is deprecated and replaced with `mainClass` since gradle 6.4
            mainClass.set("com.pinterest.ktlint.Main")
        } else {
            main = "com.pinterest.ktlint.Main"
        }
        classpath = diktatConfiguration
        project.logger.debug("Setting diktatCheck classpath to ${diktatConfiguration.dependencies.toSet()}")
        if (diktatExtension.debug) {
            logger.lifecycle("Running diktat $DIKTAT_VERSION with ktlint $KTLINT_VERSION")
        }
        args = additionalFlags.toMutableList().apply {
            if (diktatExtension.debug) {
                add("--debug")
            }
            diktatExtension
                .inputs
                .also {
                    if (it.isEmpty) {
                        /*
                         If ktlint receives empty patterns, it implicitly uses &#42;&#42;/*.kt, **/*.kts instead.
                         This can lead to diktat analyzing gradle buildscripts and so on. We want to prevent it.
                        */
                        logger.warn("Inputs for $name do not exist, will not run diktat")
                        shouldRun = false
                    }
                }
                .files
                .forEach {
                    addPattern(it)
                }
            diktatExtension.excludes?.files?.forEach {
                addPattern(it, negate = true)
            }
        }
        logger.debug("Setting JavaExec args to $args")
    }

    @TaskAction
    override fun exec() {
        if (shouldRun) {
            super.exec()
        } else {
            logger.info("Skipping diktat execution")
        }
    }

    /**
     * @param ignoreFailures whether failure in this plugin should be ignored by a build
     */
    override fun setIgnoreFailures(ignoreFailures: Boolean) = ignoreFailuresProp.set(ignoreFailures)

    /**
     * @return whether failure in this plugin should be ignored by a build
     */
    @Suppress("FUNCTION_BOOLEAN_PREFIX")
    override fun getIgnoreFailures(): Boolean = ignoreFailuresProp.getOrElse(false)

    @Suppress("MagicNumber")
    private fun isMainClassPropertySupported(gradleVersionString: String) =
            GradleVersion.fromString(gradleVersionString).run {
                major >= 6 && minor >= 4
            }

    private fun MutableList<String>.addPattern(pattern: File, negate: Boolean = false) {
        val path = if (pattern.isAbsolute) {
            pattern.relativeTo(project.rootDir)
        } else {
            pattern
        }.path
        add((if (negate) "!" else "") + path)
    }
}

/**
 * @param diktatExtension [DiktatExtension] with some values for task configuration
 * @param diktatConfiguration dependencies of diktat run
 * @return a [TaskProvider]
 */
fun Project.registerDiktatCheckTask(diktatExtension: DiktatExtension, diktatConfiguration: Configuration): TaskProvider<DiktatJavaExecTaskBase> =
        tasks.register(
            DIKTAT_CHECK_TASK, DiktatJavaExecTaskBase::class.java, gradle.gradleVersion,
            diktatExtension, diktatConfiguration
        )

/**
 * @param diktatExtension [DiktatExtension] with some values for task configuration
 * @param diktatConfiguration dependencies of diktat run
 * @return a [TaskProvider]
 */
fun Project.registerDiktatFixTask(diktatExtension: DiktatExtension, diktatConfiguration: Configuration): TaskProvider<DiktatJavaExecTaskBase> =
        tasks.register(
            DIKTAT_FIX_TASK, DiktatJavaExecTaskBase::class.java, gradle.gradleVersion,
            diktatExtension, diktatConfiguration, listOf("-F ")
        )
