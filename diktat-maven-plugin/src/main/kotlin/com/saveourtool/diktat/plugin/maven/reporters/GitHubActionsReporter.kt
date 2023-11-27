package com.saveourtool.diktat.plugin.maven.reporters

import com.saveourtool.diktat.api.DiktatReporterCreationArguments
import com.saveourtool.diktat.api.DiktatReporterType
import com.saveourtool.diktat.plugin.maven.defaultReportLocation
import org.apache.maven.project.MavenProject
import java.io.File
import java.nio.file.Path

/**
 * GitHub actions reporter
 */
class GitHubActionsReporter : Reporter {
    override fun getOutput(project: MavenProject): File = project.defaultReportLocation(DiktatReporterType.SARIF)
    override fun toCreationArguments(project: MavenProject, sourceRootDir: Path): DiktatReporterCreationArguments =
        DiktatReporterCreationArguments(
            reporterType = DiktatReporterType.SARIF,
            outputStream = getOutputStream(project),
            sourceRootDir = sourceRootDir,
        )
}
