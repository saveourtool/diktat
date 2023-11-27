/**
 * All default reporters
 */

package com.saveourtool.diktat.plugin.maven.reporters

import com.saveourtool.diktat.api.DiktatReporterCreationArguments
import com.saveourtool.diktat.api.DiktatReporterType
import com.saveourtool.diktat.plugin.maven.defaultReportLocation
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import java.io.File
import java.nio.file.Path

/**
 * A base interface for a default reporter
 *
 * @param type type of reporter
 */
open class DefaultReporter(
    private val type: DiktatReporterType,
) : Reporter {
    /**
     * Location for output
     */
    @Parameter
    var output: File? = null

    override fun getOutput(project: MavenProject): File? = output ?: project.defaultReportLocation(type)

    override fun toCreationArguments(
        project: MavenProject,
        sourceRootDir: Path,
    ): DiktatReporterCreationArguments = DiktatReporterCreationArguments(
        reporterType = type,
        outputStream = getOutputStream(project),
        sourceRootDir = sourceRootDir.takeIf { type == DiktatReporterType.SARIF },
    )
}

/**
 * Plain reporter
 */
class PlainReporter : DefaultReporter(
    type = DiktatReporterType.PLAIN,
) {
    /**
     * Plain reporter prints to stdout by default
     */
    override fun getOutput(project: MavenProject): File? = output
}

/**
 * JSON reporter
 */
class JsonReporter : DefaultReporter(
    type = DiktatReporterType.JSON,
)

/**
 * SARIF reporter
 */
class SarifReporter : DefaultReporter(
    type = DiktatReporterType.SARIF,
)

/**
 * Checkstyle reporter
 */
class CheckstyleReporter : DefaultReporter(
    type = DiktatReporterType.CHECKSTYLE,
)

/**
 * HTML reporter
 */
class HtmlReporter : DefaultReporter(
    type = DiktatReporterType.HTML,
)
