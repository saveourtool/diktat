/**
 * Configuration for project versioning
 */

package com.saveourtool.diktat.buildutils

import org.ajoberstar.reckon.core.Scope
import org.ajoberstar.reckon.core.VersionTagParser
import org.ajoberstar.reckon.gradle.ReckonExtension
import org.ajoberstar.reckon.gradle.ReckonPlugin
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

/**
 * Configures reckon plugin for [this] project, should be applied for root project only
 */
fun Project.configureVersioning() {
    apply<ReckonPlugin>()

    // should be provided in the gradle.properties
    configure<ReckonExtension> {
        setDefaultInferredScope(Scope.MINOR.name)
        stages("rc", "final")
        setScopeCalc(calcScopeFromProp())
        setStageCalc(calcStageFromProp())
    }

    val status = FileRepositoryBuilder()
        .findGitDir(project.rootDir)
        .setup()
        .let(::FileRepository)
        .let(::Git)
        .status()
        .call()

    if (!status.isClean) {
        logger.warn("git tree is not clean; " +
                "Untracked files: ${status.untracked}, uncommitted changes: ${status.uncommittedChanges}"
        )
    }
}
