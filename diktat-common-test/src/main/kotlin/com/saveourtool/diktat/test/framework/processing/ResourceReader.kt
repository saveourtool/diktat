package com.saveourtool.diktat.test.framework.processing

import com.saveourtool.diktat.test.framework.util.readTextOrNull
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.isRegularFile
import kotlin.io.path.toPath
import kotlin.io.path.writeText

/**
 * A base interface to read resources for testing purposes
 */
fun interface ResourceReader : Function1<String, Path?> {
    /**
     * @param resourceName
     * @return [Path] for provider [resourceName]
     */
    override fun invoke(resourceName: String): Path?

    companion object {
        private val log = KotlinLogging.logger {}

        /**
         * Default implementation of [ResourceReader]
         */
        val default: ResourceReader = ResourceReader { resourceName ->
            ResourceReader::class.java
                .classLoader
                .getResource(resourceName)
                ?.toURI()
                ?.toPath()
                .also { path ->
                    if (path == null || !path.isRegularFile()) {
                        log.error { "Not able to find file for running test: $resourceName" }
                    }
                }
        }

        /**
         * @param tempDir the temporary directory (usually injected by _JUnit_).
         * @param replacements a map of replacements which will be applied to actual and expected content before comparing.
         * @return Instance of [ResourceReader] with replacements of content
         */
        fun ResourceReader.withReplacements(
            tempDir: Path,
            replacements: Map<String, String>,
        ): ResourceReader = ResourceReader { resourceName ->
            this@withReplacements.invoke(resourceName)
                ?.let { originalFile ->
                    tempDir.resolve(resourceName)
                        .also { resultFile ->
                            originalFile.readTextOrNull()?.replaceAll(replacements)
                                ?.let {
                                    resultFile.parent.createDirectories()
                                    resultFile.writeText(it)
                                }
                        }
                }
        }

        /**
         * @param resourceFilePath a prefix for loading resources
         * @return Instance of [ResourceReader] which loads resource with [resourceFilePath] as prefix
         */
        fun ResourceReader.withPrefix(
            resourceFilePath: String,
        ): ResourceReader = ResourceReader { resourceName -> this@withPrefix.invoke("$resourceFilePath/$resourceName") }

        private fun String.replaceAll(replacements: Map<String, String>): String = replacements.entries
            .fold(this) { result, replacement ->
                result.replace(replacement.key, replacement.value)
            }
    }
}
