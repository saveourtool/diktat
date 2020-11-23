package org.cqfn.diktat.plugin.gradle

import generated.KTLINT_VERSION
import generated.DIKTAT_VERSION
import org.cqfn.diktat.plugin.gradle.DiktatGradlePlugin.Companion.DIKTAT_CHECK_TASK
import org.cqfn.diktat.plugin.gradle.DiktatGradlePlugin.Companion.DIKTAT_FIX_TASK
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.VerificationTask
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
            add(diktatExtension.inputs.files.joinToString { it.path })
            if (diktatExtension.excludes?.files?.isNotEmpty() == true) {
                add(diktatExtension.excludes!!.files.joinToString { "!${it.path}" })
            }
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
