@file:Suppress("DSL_SCOPE_VIOLATION")// Without suppressing these, version catalog usage in `plugins` is marked as an error in IntelliJ:
// https://youtrack.jetbrains.com/issue/KTIJ-19369


plugins {
    id(libs.plugins.kotlinJvm.get().pluginId)
}
