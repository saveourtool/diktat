package com.saveourtool.diktat.plugin.maven.reporters

import com.saveourtool.diktat.api.DiktatReporterCreationArguments
import org.apache.maven.project.MavenProject
import java.io.File
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path

/**
 * A base interface for reporter
 */
interface Reporter {
    /**
     * @param project
     * @return location as a [File] for output or default value resolved by [project]
     */
    fun getOutput(project: MavenProject): File?

    /**
     * @param project
     * @return location as an [OutputStream] for output or default value resolved by [project]
     */
    fun getOutputStream(project: MavenProject): OutputStream? = getOutput(project)?.also { Files.createDirectories(it.parentFile.toPath()) }?.outputStream()

    /**
     * @param project
     * @param sourceRootDir
     * @return [DiktatReporterCreationArguments] to create this reporter
     */
    fun toCreationArguments(project: MavenProject, sourceRootDir: Path): DiktatReporterCreationArguments
}
