package org.cqfn.diktat.plugin.gradle

import com.pinterest.ktlint.reporter.plain.PlainReporter
import groovy.lang.Closure
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency

@Suppress("unused", "MagicNumber")
class DiktatGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val diktatExtension = project.extensions.create(DIKTAT_EXTENSION, DiktatExtension::class.java)
        diktatExtension.inputs = project.fileTree("src").apply {
            include("**/*.kt")
        }
        diktatExtension.reporter = PlainReporter(System.out)

        val gradleVersion = GradleVersion.fromString(project.gradle.gradleVersion)

        // only gradle 7+ (or maybe 6.8) will embed kotlin 1.4+, kx.serialization is incompatible with kotlin 1.3, so until then we have to use JavaExec wrapper
        if (gradleVersion.major < 6 || gradleVersion.major == 6 && gradleVersion.minor < 8) {
            // configuration to provide JavaExec with correct classpath
            val diktatConfiguration = project.configurations.create(DIKTAT_CONFIGURATION) { configuration ->
                configuration.isVisible = false
                configuration.dependencies.add(project.dependencies.create("org.jetbrains.kotlin:kotlin-stdlib:1.4.10"))
                configuration.dependencies.add(project.dependencies.create("com.pinterest:ktlint:0.39.0", closureOf<ExternalModuleDependency> {
                    exclude(mutableMapOf(
                        "group" to "com.pinterest.ktlint",
                        "module" to "ktlint-ruleset-standard"
                    ))
                }))
                configuration.dependencies.add(project.dependencies.create("org.cqfn.diktat:diktat-rules:0.1.4-SNAPSHOT"))
            }

            project.registerDiktatCheckTask(diktatExtension, diktatConfiguration)
            project.registerDiktatFixTask(diktatExtension, diktatConfiguration)
        }
    }

    companion object {
        const val DIKTAT_EXTENSION = "diktat"
        const val DIKTAT_CHECK_TASK = "diktatCheck"
        const val DIKTAT_FIX_TASK = "diktatFix"
        const val DIKTAT_CONFIGURATION = "diktat"
    }
}

private data class GradleVersion(val major: Int, val minor: Int, val patch: Int) {
    companion object {
        fun fromString(version: String): GradleVersion {
            val digits = version.split('.', limit = 3).map { it.toInt() }
            return GradleVersion(digits.first(), digits.getOrElse(1) { 0 }, digits.getOrElse(2) { 0 })
        }
    }
}

// These two are copy-pasted from `kotlin-dsl` plugin's groovy interop.
// Because `kotlin-dsl` depends on kotlin 1.3.x.
fun <T> Any.closureOf(action: T.() -> Unit): Closure<Any?> =
    KotlinClosure1(action, this, this)

class KotlinClosure1<in T : Any?, V : Any>(
    val function: T.() -> V?,
    owner: Any? = null,
    thisObject: Any? = null
) : Closure<V?>(owner, thisObject) {

    @Suppress("unused") // to be called dynamically by Groovy
    fun doCall(it: T): V? = it.function()
}
