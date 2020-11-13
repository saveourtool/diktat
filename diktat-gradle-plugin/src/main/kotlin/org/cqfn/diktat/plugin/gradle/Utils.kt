/**
 * Utilities for diktat gradle plugin
 */

package org.cqfn.diktat.plugin.gradle

import groovy.lang.Closure

/**
 * A wrapper for gradle version, which supports three-digit versions like `6.6.1` or `6.0`
 * with possible suffix: `6.6.0-M1`, `6.6-RC`
 *
 * @property major major version
 * @property minor minor version
 * @property patch patch version
 * @property suffix version suffix
 */
internal data class GradleVersion(
        val major: Int,
        val minor: Int,
        val patch: Int,
        val suffix: String?) {
    companion object {
        /**
         * @param version string representation of version, e.g. from [org.gradle.api.invocation.Gradle.getGradleVersion]
         * @return a [GradleVersion]
         */
        fun fromString(version: String): GradleVersion {
            val (versionDigits, suffix) = version.split('-', limit = 2).let { splits ->
                splits.first() to splits.getOrNull(1)
            }
            val digits = versionDigits.split('.', limit = 3).map { it.toInt() }
            return GradleVersion(digits.first(), digits.getOrElse(1) { 0 }, digits.getOrElse(2) { 0 }, suffix)
        }
    }
}

// These two are copy-pasted from `kotlin-dsl` plugin's groovy interop.
// Because `kotlin-dsl` depends on kotlin 1.3.x.
@Suppress("MISSING_KDOC_TOP_LEVEL", "MISSING_KDOC_ON_FUNCTION", "KDOC_WITHOUT_PARAM_TAG", "KDOC_WITHOUT_RETURN_TAG")
fun <T> Any.closureOf(action: T.() -> Unit): Closure<Any?> =
        KotlinClosure1(action, this, this)

// GENERIC_NAME is suppressed until https://github.com/cqfn/diKTat/issues/493
@Suppress("MISSING_KDOC_TOP_LEVEL", "MISSING_KDOC_CLASS_ELEMENTS", "GENERIC_NAME", "KDOC_NO_CONSTRUCTOR_PROPERTY",
        "MISSING_KDOC_ON_FUNCTION", "KDOC_WITHOUT_PARAM_TAG", "KDOC_WITHOUT_RETURN_TAG")
class KotlinClosure1<in T : Any?, V : Any>(
        val function: T.() -> V?,
        owner: Any? = null,
        thisObject: Any? = null
) : Closure<V?>(owner, thisObject) {
    @Suppress("unused")  // to be called dynamically by Groovy
    fun doCall(it: T): V? = it.function()
}
