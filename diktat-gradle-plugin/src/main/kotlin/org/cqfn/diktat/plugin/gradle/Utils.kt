package org.cqfn.diktat.plugin.gradle

import groovy.lang.Closure

internal data class GradleVersion(val major: Int, val minor: Int, val patch: Int) {
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
