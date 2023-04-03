import org.cqfn.diktat.buildutils.configureSigning
import org.jetbrains.kotlin.incremental.createDirectory
import java.nio.file.Files

@Suppress("DSL_SCOPE_VIOLATION", "RUN_IN_SCRIPT")  // https://github.com/gradle/gradle/issues/22797
plugins {
    id("org.cqfn.diktat.buildutils.versioning-configuration")
    id("org.cqfn.diktat.buildutils.git-hook-configuration")
    id("org.cqfn.diktat.buildutils.code-quality-convention")
    id("org.cqfn.diktat.buildutils.publishing-configuration")
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

configureSigning()

tasks.create("generateLibsForDiktatSnapshot") {
    val dir = rootProject.buildDir.resolve("diktat-snapshot")

    val dependencies = setOf(
        rootProject.project(":diktat-api"),
        rootProject.project(":diktat-ktlint-engine"),
        rootProject.project(":diktat-common"),
        rootProject.project(":diktat-rules"),
        rootProject.project(":diktat-gradle-plugin"),
    )
    mustRunAfter(dependencies.map { "${it.path}:publishToMavenLocal" })
    val libsFile = rootProject.file("gradle/libs.versions.toml")

    inputs.file(libsFile)
    inputs.files(dependencies.map { it.pomFile() })
    inputs.files(dependencies.map { it.artifactFile() })
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
                val libsFileForDiktatSnapshot = dir.resolve("libs.versions.toml_snapshot")
                Files.write(libsFileForDiktatSnapshot.toPath(), it)
            }

        dependencies.forEach { dependency ->
            val artifactDir = dir.pathToMavenArtifact(dependency)
                .also { it.createDirectory() }
            Files.copy(dependency.pomFile().toPath(), artifactDir.resolve(dependency.pomFileName()).toPath())
            Files.copy(dependency.artifactFile().toPath(), artifactDir.resolve(dependency.artifactFileName()).toPath())
        }
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
fun Project.pomFile(): File = buildDir.resolve("publications")
    .let { publicationsDir ->
        publicationsDir.resolve("pluginMaven")
            .takeIf { it.exists() }
            ?: publicationsDir.resolve("maven")
    }
    .resolve("pom-default.xml")

/**
 * @return file name of pom.xml for project
 */
fun Project.pomFileName(): String = "$name-$version.pom"

/**
 * @return generated artifact for project dependency
 */
fun Project.artifactFile(): File = buildDir.resolve("libs/${artifactFileName()}")

/**
 * @return file name of artifact for project dependency
 */
fun Project.artifactFileName(): String = "$name-$version.jar"
