package org.cqfn.diktat.ruleset.smoke

import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProvider
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.lang.IllegalArgumentException

class RulesConfigValidationTest {
    private lateinit var file: File

    @BeforeEach
    fun `prepare temporary file`() {
        file = createTempFile()
    }

    @AfterEach
    fun `clear temporary file`() {
        file.delete()
    }

    @Test
    fun `should throw error if name is missing in Warnings`() {
        file.writeText(
            """
                |- name: MISSING_DOC_TOP_LEVEL
                |  enabled: true
                |  configuration: {}
            """.trimMargin()
        )
        val e = assertThrows<IllegalArgumentException> {
            DiktatRuleSetProvider(file.absolutePath).get()
        }
        Assertions.assertEquals("Warning name <MISSING_DOC_TOP_LEVEL> in configuration file is invalid, did you mean <MISSING_KDOC_TOP_LEVEL>?", e.message)
    }

    @Test
    fun `should throw error on invalid yml config`() {
        // fixme: jackson's exceptions are handled and not rethrown
        file.writeText(
            """
                |- name: PACKAGE_NAME_MISSING
                |  enabled: true
                |  configuration:
            """.trimMargin()
        )
        DiktatRuleSetProvider(file.absolutePath).get()
    }

    @Test
    @Disabled("https://github.com/cqfn/diKTat/issues/395")
    fun `should throw error on invalid configuration section`() {
        file.writeText(
            """
                |- name: TOO_LONG_FUNCTION
                |  enabled: true
                |  configuration:
                |    maxFunctionLength: 1o
                |    isIncludeHeader: Fslse
            """.trimMargin()
        )
        DiktatRuleSetProvider(file.absolutePath).get()
    }
}
