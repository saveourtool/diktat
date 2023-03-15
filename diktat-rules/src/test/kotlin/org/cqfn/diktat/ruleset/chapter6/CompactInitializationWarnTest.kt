package org.cqfn.diktat.ruleset.chapter6

import org.cqfn.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.constants.Warnings.COMPACT_OBJECT_INITIALIZATION
import org.cqfn.diktat.ruleset.rules.chapter6.classes.CompactInitialization
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import org.cqfn.diktat.ruleset.constants.WarningsNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class CompactInitializationWarnTest : LintTestBase(::CompactInitialization) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${CompactInitialization.NAME_ID}"

    @Test
    @Tag(WarningNames.COMPACT_OBJECT_INITIALIZATION)
    fun `compact class instantiation - positive example`() {
        lintMethod(
            """
                |fun main() {
                |    val httpClient = HttpClient("myConnection")
                |            .apply {
                |                url = "http://example.com"
                |                port = "8080"
                |                timeout = 100
                |            }
                |    httpClient.doRequest()
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.COMPACT_OBJECT_INITIALIZATION)
    fun `should suggest compact class instantiation`() {
        lintMethod(
            """
                |fun main() {
                |    val httpClient = HttpClient("myConnection")
                |    httpClient.url = "http://example.com"
                |    httpClient.port = "8080"
                |    httpClient.timeout = 100
                |    httpClient.doRequest()
                |}
            """.trimMargin(),
            LintError(3, 5, ruleId, "${COMPACT_OBJECT_INITIALIZATION.warnText()} url", true),
            LintError(4, 5, ruleId, "${COMPACT_OBJECT_INITIALIZATION.warnText()} port", true),
            LintError(5, 5, ruleId, "${COMPACT_OBJECT_INITIALIZATION.warnText()} timeout", true),
        )
    }

    @Test
    @Tag(WarningNames.COMPACT_OBJECT_INITIALIZATION)
    fun `should suggest compact class instantiation - with comments`() {
        lintMethod(
            """
                |fun main() {
                |    val httpClient = HttpClient("myConnection")
                |    // setting url for http requests
                |    httpClient.url = "http://example.com"
                |    // setting port
                |    httpClient.port = "8080"
                |
                |    // setting timeout to 100
                |    httpClient.timeout = 100
                |    httpClient.doRequest()
                |}
            """.trimMargin(),
            LintError(4, 5, ruleId, "${COMPACT_OBJECT_INITIALIZATION.warnText()} url", true),
            LintError(6, 5, ruleId, "${COMPACT_OBJECT_INITIALIZATION.warnText()} port", true),
            LintError(9, 5, ruleId, "${COMPACT_OBJECT_INITIALIZATION.warnText()} timeout", true),
        )
    }

    @Test
    @Tag(WarningNames.COMPACT_OBJECT_INITIALIZATION)
    fun `class instantiation partially in apply`() {
        lintMethod(
            """
                |fun main() {
                |    val httpClient = HttpClient("myConnection")
                |            .apply {
                |                url = "http://example.com"
                |                port = "8080"
                |            }
                |    httpClient.timeout = 100
                |    httpClient.doRequest()
                |}
            """.trimMargin(),
            LintError(7, 5, ruleId, "${COMPACT_OBJECT_INITIALIZATION.warnText()} timeout", true)
        )
    }

    // Creating the `apply` block here breaks the compilation
    @Test
    @Tag(WarningNames.COMPACT_OBJECT_INITIALIZATION)
    fun `should not trigger to infix function 1`() {
        lintMethod(
            """
                |fun foo(line: String) = line
                |    .split(",", ", ")
                |    .associate {
                |        val pair = it.split("=", limit = 2).map {
                |            it.replace("\\=", "=")
                |        }
                |        pair.first() to pair.last()
                |    }
            """.trimMargin(),
        )
    }

    // Apply block doesn't break the compilation, however such changes can break the user logic
    @Test
    @Tag(WarningNames.COMPACT_OBJECT_INITIALIZATION)
    fun `should not trigger to infix function 2`() {
        lintMethod(
            """
                |fun foo(line: String) {
                |    val pair = line.split("=", limit = 2).map {
                |        it.replace("\\=", "=")
                |    }
                |    pair.first() to pair.last()
                |}
            """.trimMargin(),
        )
    }

    // For generality don't trigger on any infix function, despite the fact, that with apply block all will be okay
    @Test
    @Tag(WarningNames.COMPACT_OBJECT_INITIALIZATION)
    fun `should not trigger to infix function 3`() {
        lintMethod(
            """
                |fun `translate text`() {
                |    val res = translateText(text = "dummy")
                |    (res is TranslationsSuccess) shouldBe true
                |    val translationsSuccess = res as TranslationsSuccess
                |    translationsSuccess.translations shouldHaveSize 1
                |}
            """.trimMargin(),
        )
    }
}
