package org.cqfn.diktat.plugin.gradle

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

open class DiktatJavaExecTaskBase @Inject constructor(
    diktatExtension: DiktatExtension,
    diktatConfiguration: Configuration,
    additionalFlags: Iterable<String> = emptyList()
    ) : JavaExec(), VerificationTask {
    init {
        group = "verification"
        mainClass.set("com.pinterest.ktlint.Main")
        classpath = diktatConfiguration
        project.logger.debug("Setting diktatCheck classpath to ${diktatConfiguration.dependencies.toSet()}")
        setArgs(additionalFlags.toMutableList().apply { add(diktatExtension.inputs.files.joinToString { it.path }) })
    }

    @get:Internal
    internal val ignoreFailuresProp: Property<Boolean> = project.objects.property(Boolean::class.javaObjectType)

    override fun setIgnoreFailures(ignoreFailures: Boolean) = ignoreFailuresProp.set(ignoreFailures)

    override fun getIgnoreFailures(): Boolean = ignoreFailuresProp.getOrElse(false)
}

fun Project.registerDiktatCheckTask(diktatExtension: DiktatExtension, diktatConfiguration: Configuration): TaskProvider<DiktatJavaExecTaskBase> =
    tasks.register(
        DIKTAT_CHECK_TASK, DiktatJavaExecTaskBase::class.java,
        diktatExtension, diktatConfiguration, listOf(if (diktatExtension.debug) "--debug " else "")
    )

fun Project.registerDiktatFixTask(diktatExtension: DiktatExtension, diktatConfiguration: Configuration): TaskProvider<DiktatJavaExecTaskBase> =
    tasks.register(
        DIKTAT_FIX_TASK, DiktatJavaExecTaskBase::class.java,
        diktatExtension, diktatConfiguration, listOf("-F ", if (diktatExtension.debug) "--debug " else "")
    )
