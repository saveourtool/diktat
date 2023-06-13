package com.saveourtool.diktat.buildutils

/**
 * Task of type [Copy] that install git hooks from directory in repo to .git directory
 */
val installGitHooksTask = tasks.register("installGitHooks", Copy::class) {
    from(file("$rootDir/.git-hooks"))
    into(file("$rootDir/.git/hooks"))
}

// add git hooks installation to build by adding it as a dependency for some common task
run {
    tasks.findByName("build")?.dependsOn(installGitHooksTask)
}
