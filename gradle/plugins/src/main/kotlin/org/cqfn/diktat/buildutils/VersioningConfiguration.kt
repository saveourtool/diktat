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

    // should be provided in the gradle.properties
    configure<ReckonExtension> {
        snapshots()
        setScopeCalc(calcScopeFromProp())
        setStageCalc(calcStageFromProp())
    }

    val status = project.extensions.getByType<GrgitServiceExtension>()
        .service
        .map { it.grgit.repository.jgit.status().call() }
        .get()
    if (!status.isClean) {
        logger.warn("git tree is not clean; " +
                "Untracked files: ${status.untracked}, uncommitted changes: ${status.uncommittedChanges}"
        )
    }
}
