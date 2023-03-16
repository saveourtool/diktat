/**
 * Configuration for project versioning
 */

package org.cqfn.diktat.buildutils

import org.ajoberstar.grgit.gradle.GrgitServiceExtension
import org.ajoberstar.grgit.gradle.GrgitServicePlugin
import org.ajoberstar.reckon.gradle.ReckonExtension
import org.ajoberstar.reckon.gradle.ReckonPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

/**
 * Configures reckon plugin for [this] project, should be applied for root project only
 */
fun Project.configureVersioning() {
    apply<ReckonPlugin>()
    apply<GrgitServicePlugin>()
    val grgitProvider = project.extensions.getByType<GrgitServiceExtension>()
        .service
        .map { it.grgit }

    // should be provided in the gradle.properties
    configure<ReckonExtension> {
        scopeFromProp()
        // this should be used during local development most of the time, so that constantly changing version
        // on a dirty git tree doesn't cause other task updates
        snapshotFromProp()
    }

    val grgit = grgitProvider.get()
    val status = grgit.repository.jgit.status()
        .call()
    if (!status.isClean) {
        logger.warn("git tree is not clean; " +
                "Untracked files: ${status.untracked}, uncommitted changes: ${status.uncommittedChanges}"
        )
    }
}

/**
 * @return true if this string denotes a snapshot version
 */
internal fun String.isSnapshot() = endsWith("SNAPSHOT")
