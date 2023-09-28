package com.saveourtool.diktat.buildutils

// FixMe: remove after 2.0.0
run {
    @Suppress("RUN_IN_SCRIPT", "AVOID_NULL_CHECKS")
    plugins {
        id("com.saveourtool.diktat.buildutils.detekt-convention-configuration")
        id("com.saveourtool.diktat.buildutils.diktat-convention-configuration")
    }
}
