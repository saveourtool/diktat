package com.saveourtool.diktat.plugin.gradle.extensions

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile

abstract class Reporter {
    @get:Input
    abstract val id: Property<String>

    @get:OutputFile
    abstract val output: RegularFileProperty
}
