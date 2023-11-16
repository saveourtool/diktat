package com.saveourtool.diktat.plugin.gradle.extensions

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

abstract class Reporters @Inject constructor(
    private val objectFactory: ObjectFactory,
    private val values: MutableList<Reporter>,
) {
    fun configure(action: Action<in Reporter>): Unit =
            action.execute(objectFactory.newInstance(Reporter::class.java).apply { values.add(this) })
}
