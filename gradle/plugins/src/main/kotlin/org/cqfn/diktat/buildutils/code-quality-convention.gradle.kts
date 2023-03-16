package org.cqfn.diktat.buildutils

plugins {
    id("org.cqfn.diktat.buildutils.detekt-convention-configuration")
    id("org.cqfn.diktat.buildutils.diktat-convention-configuration")
    id("org.cqfn.diktat.buildutils.spotless-convention-configuration")
}

// FixMe: only registers the task, doesn't actually install them
installGitHooks()
