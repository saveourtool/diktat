/**
 * Publishing configuration file.
 */

@file:Suppress(
    "MISSING_KDOC_TOP_LEVEL",
    "MISSING_KDOC_ON_FUNCTION",
)

package com.saveourtool.diktat.buildutils

import io.github.gradlenexus.publishplugin.NexusPublishExtension
import io.github.gradlenexus.publishplugin.NexusPublishPlugin
import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.tasks.bundling.Jar
import org.gradle.internal.logging.text.StyledTextOutput
import org.gradle.internal.logging.text.StyledTextOutput.Style.Failure
import org.gradle.internal.logging.text.StyledTextOutput.Style.Success
import org.gradle.internal.logging.text.StyledTextOutputFactory
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin
import org.jetbrains.dokka.gradle.DokkaPlugin

/**
 * Configures all aspects of the publishing process.
 */
fun Project.configurePublishing() {
    apply<MavenPublishPlugin>()
    if (this == rootProject) {
        configureNexusPublishing()
        configureGitHubPublishing()
    }

    afterEvaluate {
        configureSigning()
    }
}

/**
 * Configures _pom.xml_
 *
 * @param project
 */
@Suppress("TOO_LONG_FUNCTION")
fun MavenPom.configurePom(project: Project) {
    name.set(project.name)
    description.set(project.description ?: project.name)
    url.set("https://github.com/saveourtool/diktat")
    licenses {
        license {
            name.set("MIT License")
            url.set("https://opensource.org/license/MIT")
            distribution.set("repo")
        }
    }
    developers {
        developer {
            id.set("akuleshov7")
            name.set("Andrey Kuleshov")
            email.set("andrewkuleshov7@gmail.com")
            url.set("https://github.com/akuleshov7")
        }
        developer {
            id.set("petertrr")
            name.set("Peter Trifanov")
            email.set("peter.trifanov@gmail.com")
            url.set("https://github.com/petertrr")
        }
        developer {
            id.set("nulls")
            name.set("Nariman Abdullin")
            email.set("nulls.narik@gmail.com")
            url.set("https://github.com/nulls")
        }
    }
    scm {
        url.set("https://github.com/saveourtool/diktat")
        connection.set("scm:git:git://github.com/saveourtool/diktat.git")
        developerConnection.set("scm:git:git@github.com:saveourtool/diktat.git")
    }
}

/**
 * Configures all publications. The publications must already exist.
 */
@Suppress("TOO_LONG_FUNCTION")
fun Project.configurePublications() {
    if (this == rootProject) {
        return
    }
    val sourcesJar = tasks.named(SOURCES_JAR)
    apply<DokkaPlugin>()
    @Suppress("GENERIC_VARIABLE_WRONG_DECLARATION")
    val dokkaJarProvider = tasks.register<Jar>("dokkaJar") {
        group = "documentation"
        archiveClassifier.set("javadoc")
        from(tasks.named("dokkaHtml"))
    }
    configure<PublishingExtension> {
        repositories {
            mavenLocal()
        }
        publications.withType<MavenPublication>().configureEach {
            artifact(sourcesJar)
            artifact(dokkaJarProvider)
            pom {
                configurePom(project)
            }
        }
    }
}

/**
 * Configures Maven Central as the publish destination.
 */
@Suppress("TOO_LONG_FUNCTION")
private fun Project.configureNexusPublishing() {
    setPropertyFromEnv("OSSRH_USERNAME", "sonatypeUsername")
    setPropertyFromEnv("OSSRH_PASSWORD", "sonatypePassword")

    if (!hasProperties("sonatypeUsername", "sonatypePassword")) {
        styledOut(logCategory = "nexus")
            .style(StyledTextOutput.Style.Info)
            .text("Skipping Nexus publishing configuration as either ")
            .style(StyledTextOutput.Style.Identifier)
            .text("sonatypeUsername")
            .style(StyledTextOutput.Style.Info)
            .text(" or ")
            .style(StyledTextOutput.Style.Identifier)
            .text("sonatypePassword")
            .style(StyledTextOutput.Style.Info)
            .text(" are not set")
            .println()
        return
    }

    apply<NexusPublishPlugin>()

    configure<NexusPublishExtension> {
        repositories {
            sonatype {
                /*
                 * The default is https://oss.sonatype.org/service/local/.
                 */
                nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
                /*
                 * The default is https://oss.sonatype.org/content/repositories/snapshots/.
                 */
                snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
                username.set(property("sonatypeUsername") as String)
                password.set(property("sonatypePassword") as String)
            }
        }
    }
}

/**
 * Configures GitHub Packages as the publish destination.
 */
private fun Project.configureGitHubPublishing() {
    configure<PublishingExtension> {
        repositories {
            maven {
                name = "GitHub"
                url = uri("https://maven.pkg.github.com/saveourtool/diktat")
                credentials {
                    username = findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                    password = findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
                }
            }
        }
    }
}

/**
 * Enables signing of the artifacts if the `signingKey` project property is set.
 *
 * Should be explicitly called after each custom `publishing {}` section.
 */
private fun Project.configureSigning() {
    setPropertyFromEnv("GPG_SEC", "signingKey")
    setPropertyFromEnv("GPG_PASSWORD", "signingPassword")

    if (hasProperty("signingKey")) {
        /*
         * GitHub Actions.
         */
        configureSigningCommon {
            useInMemoryPgpKeys(property("signingKey") as String?, findProperty("signingPassword") as String?)
        }
    } else if (
        this.hasProperties(
            "signing.keyId",
            "signing.password",
            "signing.secretKeyRingFile",
        )
    ) {
        /*-
         * Pure-Java signing mechanism via `org.bouncycastle.bcpg`.
         *
         * Requires an 8-digit (short form) PGP key id and a present `~/.gnupg/secring.gpg`
         * (for gpg 2.1, run
         * `gpg --keyring secring.gpg --export-secret-keys >~/.gnupg/secring.gpg`
         * to generate one).
         */
        configureSigningCommon()
    } else if (hasProperty("signing.gnupg.keyName")) {
        /*-
         * Use an external `gpg` executable.
         *
         * On Windows, you may need to additionally specify the path to `gpg` via
         * `signing.gnupg.executable`.
         */
        configureSigningCommon {
            useGpgCmd()
        }
    }
}

/**
 * @param useKeys the block which configures the PGP keys. Use either
 *   [SigningExtension.useInMemoryPgpKeys], [SigningExtension.useGpgCmd], or an
 *   empty lambda.
 * @see SigningExtension.useInMemoryPgpKeys
 * @see SigningExtension.useGpgCmd
 */
private fun Project.configureSigningCommon(useKeys: SigningExtension.() -> Unit = {}) {
    apply<SigningPlugin>()
    configure<SigningExtension> {
        useKeys()
        val publications = extensions.getByType<PublishingExtension>().publications
        val publicationCount = publications.size
        val message = "The following $publicationCount publication(s) are getting signed: ${publications.map(Named::getName)}"
        val style = when (publicationCount) {
            0 -> Failure
            else -> Success
        }
        styledOut(logCategory = "signing").style(style).println(message)
        sign(*publications.toTypedArray())
    }
    tasks.withType<PublishToMavenRepository>().configureEach {
        // Workaround for the problem described at https://github.com/saveourtool/save-cli/pull/501#issuecomment-1439705340.
        // We have a single Javadoc artifact shared by all platforms, hence all publications depend on signing of this artifact.
        // This causes weird implicit dependencies, like `publishJsPublication...` depends on `signJvmPublication`.
        dependsOn(tasks.withType<Sign>())
    }
}

/**
 * Creates a styled text output.
 *
 * @param logCategory
 * @return [StyledTextOutput]
 */
private fun Project.styledOut(logCategory: String): StyledTextOutput = serviceOf<StyledTextOutputFactory>().create(logCategory)

/**
 * Determines if this project has all the given properties.
 *
 * @param propertyNames the names of the properties to locate.
 * @return `true` if this project has all the given properties, `false` otherwise.
 * @see Project.hasProperty
 */
private fun Project.hasProperties(vararg propertyNames: String): Boolean = propertyNames.asSequence().all(this::hasProperty)

private fun Project.setPropertyFromEnv(envName: String, propertyName: String) {
    System.getenv(envName)?.let {
        extra.set(propertyName, it)
    }
}
