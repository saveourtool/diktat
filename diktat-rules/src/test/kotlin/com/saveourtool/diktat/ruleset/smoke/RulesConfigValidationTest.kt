package com.saveourtool.diktat.ruleset.smoke

import com.saveourtool.diktat.ruleset.config.DiktatRuleConfigYamlReader
import com.saveourtool.diktat.ruleset.rules.DiktatRuleSetFactoryImpl
import com.saveourtool.diktat.test.framework.util.deleteIfExistsSilently

import com.charleskorn.kaml.InvalidPropertyValueException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

import java.io.File
import java.lang.IllegalArgumentException
import kotlin.io.path.createTempFile

class RulesConfigValidationTest {
    private lateinit var file: File

    @BeforeEach
    fun setUp() {
        file = createTempFile().toFile()
    }

    @AfterEach
    fun tearDown() {
        file.toPath().deleteIfExistsSilently()
    }

    @Test
    @Suppress("GENERIC_VARIABLE_WRONG_DECLARATION")
    fun `should throw error if name is missing in Warnings`() {
        file.writeText(
            """
                |- name: MISSING_DOC_TOP_LEVEL
                |  enabled: true
                |  configuration: {}
            """.trimMargin()
        )
        val exception = assertThrows<IllegalArgumentException> {
            diktatRuleSetFactory(diktatRuleConfigReader(file.inputStream()))
        }
        Assertions.assertEquals("Warning name <MISSING_DOC_TOP_LEVEL> in configuration file is invalid, did you mean <MISSING_KDOC_TOP_LEVEL>?", exception.message)
    }

    @Test
    fun `should throw error on invalid yml config`() {
        file.writeText(
            """
                |- name: PACKAGE_NAME_MISSING
                |  enabled: true
                |  configuration:
            """.trimMargin()
        )
        assertThrows<InvalidPropertyValueException> {
            diktatRuleSetFactory(diktatRuleConfigReader(file.inputStream()))
        }
    }

    @Test
    @Disabled("https://github.com/saveourtool/diKTat/issues/395")
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
        diktatRuleSetFactory(diktatRuleConfigReader(file.inputStream()))
    }

    companion object {
        private val diktatRuleSetFactory = DiktatRuleSetFactoryImpl()
        private val diktatRuleConfigReader = DiktatRuleConfigYamlReader()
    }
}
