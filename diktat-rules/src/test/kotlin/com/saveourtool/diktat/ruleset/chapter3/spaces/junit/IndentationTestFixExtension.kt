package com.saveourtool.diktat.ruleset.chapter3.spaces.junit

import com.saveourtool.diktat.ruleset.chapter3.spaces.asRulesConfigList
import com.saveourtool.diktat.ruleset.chapter3.spaces.withCustomParameters
import com.saveourtool.diktat.ruleset.junit.CloseablePath
import com.saveourtool.diktat.ruleset.rules.chapter3.files.IndentationRule
import com.saveourtool.diktat.util.FixTestBase
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Namespace
import java.nio.file.Path
import kotlin.io.path.createTempDirectory

/**
 * The `Extension` implementation for indentation test templates (fix mode).
 *
 * @property customConfig non-default configuration for the indentation rule.
 * @property actualCode the original file content (may well get modified as
 *   fixes are applied).
 */
@Suppress(
    "TOO_MANY_BLANK_LINES",  // Readability
    "WRONG_INDENTATION")  // False positives, see #1404.
class IndentationTestFixExtension(
    override val customConfig: Map<String, Any>,
    @Language("kotlin") override val actualCode: String,
    @Language("kotlin") private val expectedCode: String
) : FixTestBase("nonexistent", ::IndentationRule),
    IndentationTestExtension,
    BeforeEachCallback {

    private lateinit var tempDir: Path

    override fun beforeEach(context: ExtensionContext) {
        tempDir = context.getStore(namespace).getOrComputeIfAbsent(KEY, {
            CloseablePath(createTempDirectory(prefix = TEMP_DIR_PREFIX))
        }, CloseablePath::class.java).directory
    }

    override fun beforeTestExecution(context: ExtensionContext) {
        val lintResult = fixAndCompareContent(
            actualCode,
            expectedCode,
            tempDir,
            overrideRulesConfigList = defaultConfig.withCustomParameters(customConfig).asRulesConfigList(),
        )

        lintResult.assertSuccessful()
    }

    private companion object {
        private const val KEY = "temp.dir"
        private const val TEMP_DIR_PREFIX = "junit"
        private val namespace = Namespace.create(IndentationTestFixExtension::class)
    }
}
