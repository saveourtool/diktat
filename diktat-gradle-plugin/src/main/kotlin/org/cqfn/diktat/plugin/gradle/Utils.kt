/**
 * Utilities for diktat gradle plugin
 */

@file:Suppress("FILE_NAME_MATCH_CLASS", "MatchingDeclarationName")

package org.cqfn.diktat.plugin.gradle

import groovy.lang.Closure

@Suppress(
    "MISSING_KDOC_TOP_LEVEL",
    "MISSING_KDOC_CLASS_ELEMENTS",
    "KDOC_NO_CONSTRUCTOR_PROPERTY",
    "MISSING_KDOC_ON_FUNCTION",
    "KDOC_WITHOUT_PARAM_TAG",
    "KDOC_WITHOUT_RETURN_TAG"
)
class KotlinClosure1<in T : Any?, V : Any>(
    val function: T.() -> V?,
    owner: Any? = null,
    thisObject: Any? = null
) : Closure<V?>(owner, thisObject) {
    @Suppress("unused")  // to be called dynamically by Groovy
    fun doCall(it: T): V? = it.function()
}

// These two are copy-pasted from `kotlin-dsl` plugin's groovy interop.
// Because `kotlin-dsl` depends on kotlin 1.3.x.
@Suppress(
    "MISSING_KDOC_TOP_LEVEL",
    "MISSING_KDOC_ON_FUNCTION",
    "KDOC_WITHOUT_PARAM_TAG",
    "KDOC_WITHOUT_RETURN_TAG"
)
fun <T> Any.closureOf(action: T.() -> Unit): Closure<Any?> =
    KotlinClosure1(action, this, this)
