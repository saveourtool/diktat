package com.saveourtool.save.buildutils

import io.github.gradlenexus.publishplugin.NexusPublishPlugin
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.extra

plugins {
    `maven-publish`
    signing
}

run {
    // If present, set properties from env variables. If any are absent, release will fail.
    System.getenv("OSSRH_USERNAME")?.let {
        extra.set("sonatypeUsername", it)
    }
    System.getenv("OSSRH_PASSWORD")?.let {
        extra.set("sonatypePassword", it)
    }
    System.getenv("GPG_SEC")?.let {
        extra.set("signingKey", it)
    }
    System.getenv("GPG_PASSWORD")?.let {
        extra.set("signingPassword", it)
    }

    if (project.path == rootProject.path) {
        apply<NexusPublishPlugin>()
        if (hasProperty("sonatypeUsername")) {
            configureNexusPublishing()
        }
    }
}

run {
    configurePublications()
}
