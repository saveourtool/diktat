@file:Suppress("FILE_UNORDERED_IMPORTS")// False positives, see #1494.

package com.saveourtool.diktat.ruleset.chapter3.spaces.junit

import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig.Companion.NEWLINE_AT_END
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback
import com.saveourtool.diktat.ruleset.chapter3.spaces.IndentationConfigFactory as IndentationConfig

/**
 * The common super-interface for indentation-specific `Extension`
 * implementations.
 */
internal interface IndentationTestExtension : BeforeTestExecutionCallback {
    /**
     * The default configuration for the indentation rule.
     */
    @Suppress("CUSTOM_GETTERS_SETTERS")
    val defaultConfig
        get() =
            IndentationConfig(NEWLINE_AT_END to false)

    /**
     * Non-default configuration for the indentation rule.
     */
    val customConfig: Map<String, Any>

    /**
     * The original file content (may well get modified as fixes are applied).
     */
    @get:Language("kotlin")
    val actualCode: String
}
