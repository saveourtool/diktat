import org.cqfn.diktat.buildutils.configurePublications
import org.cqfn.diktat.buildutils.configureSigning
import org.jetbrains.kotlin.incremental.createDirectory
import java.nio.file.Files

@Suppress("DSL_SCOPE_VIOLATION", "RUN_IN_SCRIPT")  // https://github.com/gradle/gradle/issues/22797
plugins {
    id("org.cqfn.diktat.buildutils.versioning-configuration")
    id("org.cqfn.diktat.buildutils.git-hook-configuration")
    id("org.cqfn.diktat.buildutils.code-quality-convention")
    id("org.cqfn.diktat.buildutils.nexus-publishing-configuration")
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

configurePublications()
configureSigning()

tasks.create("generateLibsForDiktatSnapshot") {
    val dir = rootProject.buildDir.resolve("diktat-snapshot")

    val dependencies = setOf(
        projects.diktatCommon,
        projects.diktatRules,
        projects.diktatRunner.diktatRunnerApi,
        projects.diktatRunner.diktatRunnerKtlintEngine,
        projects.diktatGradlePlugin,
    )
    mustRunAfter(dependencies.map { ":${it.name}:publishToMavenLocal" })
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
 * @param projectDependency
 * @return resolved path to directory according to maven coordinate
 */
fun File.pathToMavenArtifact(projectDependency: ProjectDependency): File = projectDependency.group.toString()
    .split(".")
    .fold(this) { dirToArtifact, newPart -> dirToArtifact.resolve(newPart) }
    .resolve(projectDependency.name)
    .resolve(projectDependency.version.toString())

/**
 * @return generated pom.xml for project dependency
 */
fun ProjectDependency.pomFile(): File = rootProject.file("$name/build/publications/")
    .let { publicationsDir ->
        publicationsDir.resolve("pluginMaven")
            .takeIf { it.exists() }
            ?: publicationsDir.resolve("maven")
    }
    .resolve("pom-default.xml")

/**
 * @return file name of pom.xml for project dependency
 */
fun ProjectDependency.pomFileName(): String = "$name-$version.pom"

/**
 * @return generated artifact for project dependency
 */
fun ProjectDependency.artifactFile(): File = rootProject.file("$name/build/libs/$name-$version.jar")

/**
 * @return file name of artifact for project dependency
 */
fun ProjectDependency.artifactFileName(): String = "$name-$version.jar"
