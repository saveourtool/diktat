package com.saveourtool.diktat.buildutils

@Suppress("AVOID_NULL_CHECKS")
plugins {
    id("com.saveourtool.diktat.buildutils.detekt-convention-configuration")
    // FixMe: remove after 2.0.0
    if (System.getenv("DIKTAT_SNAPSHOT") != null) {
        id("com.saveourtool.diktat.buildutils.diktat-convention-configuration")
    }
}
