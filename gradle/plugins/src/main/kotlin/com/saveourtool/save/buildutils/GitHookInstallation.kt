/**
 * Gradle tasks to install git hooks as a part of the build
 */

package com.saveourtool.save.buildutils

import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.register

/**
 * Task of type [Copy] that install git hooks from directory in repo to .git directory
 */
fun Project.installGitHooks() {
    val installGitHooksTask = tasks.register("installGitHooks", Copy::class) {
        from(file("$rootDir/.git-hooks"))
        into(file("$rootDir/.git/hooks"))
    }
    // add git hooks installation to build by adding it as a dependency for some common task
    tasks.named("build") {
        dependsOn(installGitHooksTask)
    }
}
