import org.jetbrains.kotlin.incremental.createDirectory
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@Suppress("DSL_SCOPE_VIOLATION", "RUN_IN_SCRIPT")  // https://github.com/gradle/gradle/issues/22797
plugins {
    id("com.saveourtool.diktat.buildutils.versioning-configuration")
    id("com.saveourtool.diktat.buildutils.git-hook-configuration")
    id("com.saveourtool.diktat.buildutils.code-quality-convention")
    id("com.saveourtool.diktat.buildutils.publishing-configuration")
    alias(libs.plugins.talaiot.base)
    java
    `maven-publish`
}

talaiot {
    metrics {
        // disabling due to problems with OSHI on some platforms
        performanceMetrics = false
        environmentMetrics = false
    }
    publishers {
        timelinePublisher = true
    }
}

project.description = "diKTat kotlin formatter and fixer"

val libsFileName = "libs.versions.toml"
val libsFile = rootProject.file("gradle/$libsFileName")
val libsFileBackup = rootProject.file("gradle/${libsFileName}_backup")

tasks.create("generateLibsForDiktatSnapshot") {
    val dir = rootProject.layout
        .buildDirectory
        .dir("diktat-snapshot")
        .get()
        .asFile

    val dependency = rootProject.project(":diktat-gradle-plugin")
    dependsOn(dependency.let { "${it.path}:publishToMavenLocal" })

    inputs.file(libsFile)
    inputs.files(dependency.pomFile())
    inputs.files(dependency.artifactFile())
    inputs.property("project-version", version.toString())
    outputs.dir(dir)

    doFirst {
        dir.deleteRecursively()
        dir.createDirectory()
    }
    doLast {
        Files.readAllLines(libsFile.toPath())
            .map { line ->
                when {
                    line.contains("diktat = ") -> "diktat = \"$version\""
                    else -> line
                }
            }
            .let {
                val libsFileForDiktatSnapshot = dir.resolve(libsFileName)
                Files.write(libsFileForDiktatSnapshot.toPath(), it)
                Files.move(libsFile.toPath(), libsFileBackup.toPath(), StandardCopyOption.REPLACE_EXISTING)
                Files.copy(libsFileForDiktatSnapshot.toPath(), libsFile.toPath())
            }

        val artifactDir = dir.pathToMavenArtifact(dependency)
            .also { it.createDirectory() }
        Files.copy(dependency.pomFile().toPath(), artifactDir.resolve(dependency.pomFileName()).toPath())
        Files.copy(dependency.artifactFile().toPath(), artifactDir.resolve(dependency.artifactFileName()).toPath())
    }
}

tasks.create("rollbackLibsForDiktatSnapshot") {
    inputs.file(libsFileBackup)
    outputs.file(libsFile)

    doLast {
        Files.deleteIfExists(libsFile.toPath())
        Files.move(libsFileBackup.toPath(), libsFile.toPath())
    }
}

/**
 * @param project
 * @return resolved path to directory according to maven coordinate
 */
fun File.pathToMavenArtifact(project: Project): File = project.group.toString()
    .split(".")
    .fold(this) { dirToArtifact, newPart -> dirToArtifact.resolve(newPart) }
    .resolve(project.name)
    .resolve(project.version.toString())

/**
 * @return generated pom.xml for project dependency
 */
fun Project.pomFile(): File = layout.buildDirectory
    .dir("publications")
    .map { publicationsDir ->
        publicationsDir.dir("pluginMaven")
            .takeIf { it.asFile.exists() }
            ?: publicationsDir.dir("maven")
    }
    .map { it.file("pom-default.xml").asFile }
    .get()

/**
 * @return file name of pom.xml for project
 */
fun Project.pomFileName(): String = "$name-$version.pom"

/**
 * @return generated artifact for project dependency
 */
fun Project.artifactFile(): File = layout.buildDirectory
    .dir("libs")
    .map { it.file(artifactFileName()).asFile }
    .get()

/**
 * @return file name of artifact for project dependency
 */
fun Project.artifactFileName(): String = "$name-$version.jar"
