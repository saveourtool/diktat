package org.cqfn.diktat.ruleset.chapter6

import org.cqfn.diktat.common.config.rules.DIKTAT_COMMON
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.INLINE_CLASS_CAN_BE_USED
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.chapter6.classes.InlineClassesRule
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class InlineClassesWarnTest : LintTestBase(::InlineClassesRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:inline-classes"
    private val rulesConfigListEarlierVersion: List<RulesConfig> = listOf(
        RulesConfig(
            DIKTAT_COMMON, true,
            mapOf("kotlinVersion" to "1.2.9"))
    )
    private val rulesConfigListSameVersion: List<RulesConfig> = listOf(
        RulesConfig(
            DIKTAT_COMMON, true,
            mapOf("kotlinVersion" to "1.3"))
    )
    private val rulesConfigListLateVersion: List<RulesConfig> = listOf(
        RulesConfig(
            DIKTAT_COMMON, true,
            mapOf("kotlinVersion" to "1.4.30"))
    )
    private val rulesConfigListUnsupportedVersion: List<RulesConfig> = listOf(
        RulesConfig(
            DIKTAT_COMMON, true,
            mapOf("kotlinVersion" to "1.5.20"))
    )

    @Test
    @Tag(WarningNames.INLINE_CLASS_CAN_BE_USED)
    fun `should not trigger on inline class`() {
        lintMethod(
            """
                |inline class Name(val s: String) {}
            """.trimMargin(),
            rulesConfigList = rulesConfigListSameVersion
        )
    }

    @Test
    @Tag(WarningNames.INLINE_CLASS_CAN_BE_USED)
    fun `should trigger on regular class`() {
        lintMethod(
            """
                |class Some {
                |   val config = Config()
                |}
            """.trimMargin(),
            LintError(1, 1, ruleId, "${INLINE_CLASS_CAN_BE_USED.warnText()} class Some", false),
            rulesConfigList = rulesConfigListSameVersion
        )
    }

    @Test
    @Tag(WarningNames.INLINE_CLASS_CAN_BE_USED)
    fun `should trigger on class with appropriate modifiers`() {
        lintMethod(
            """
                |final class Some {
                |   val config = Config()
                |}
            """.trimMargin(),
            LintError(1, 1, ruleId, "${INLINE_CLASS_CAN_BE_USED.warnText()} class Some", false),
            rulesConfigList = rulesConfigListSameVersion
        )
    }

    @Test
    @Tag(WarningNames.INLINE_CLASS_CAN_BE_USED)
    fun `should not trigger on class with inappropriate modifiers`() {
        lintMethod(
            """
                |abstract class Some {
                |   val config = Config()
                |}
            """.trimMargin(),
            rulesConfigList = rulesConfigListSameVersion
        )
    }

    @Test
    @Tag(WarningNames.INLINE_CLASS_CAN_BE_USED)
    fun `should not trigger on interface`() {
        lintMethod(
            """
                |interface Some {
                |   val config = Config()
                |}
            """.trimMargin(),
            rulesConfigList = rulesConfigListSameVersion
        )
    }

    @Test
    @Tag(WarningNames.INLINE_CLASS_CAN_BE_USED)
    fun `should trigger on class with val prop in constructor`() {
        lintMethod(
            """
                |class Some(val anything: Int) {
                |
                |}
            """.trimMargin(),
            LintError(1, 1, ruleId, "${INLINE_CLASS_CAN_BE_USED.warnText()} class Some", false),
            rulesConfigList = rulesConfigListSameVersion
        )
    }

    @Test
    @Tag(WarningNames.INLINE_CLASS_CAN_BE_USED)
    fun `should not trigger on class with var prop #1`() {
        lintMethod(
            """
                |class Some(var anything: Int) {
                |
                |}
            """.trimMargin(),
            rulesConfigList = rulesConfigListSameVersion
        )
    }

    @Test
    @Tag(WarningNames.INLINE_CLASS_CAN_BE_USED)
    fun `should not trigger on class with var prop #2`() {
        lintMethod(
            """
                |class Some {
                |   var some = 3
                |}
            """.trimMargin(),
            rulesConfigList = rulesConfigListSameVersion
        )
    }

    @Test
    @Tag(WarningNames.INLINE_CLASS_CAN_BE_USED)
    fun `should not trigger on class that extends class`() {
        lintMethod(
            """
                |class Some : Any() {
                |   val some = 3
                |}
            """.trimMargin(),
            rulesConfigList = rulesConfigListSameVersion
        )
    }

    @Test
    @Tag(WarningNames.INLINE_CLASS_CAN_BE_USED)
    fun `should trigger on class that extends interface`() {
        lintMethod(
            """
                |class Some : Any {
                |   val some = 3
                |}
            """.trimMargin(),
            LintError(1, 1, ruleId, "${INLINE_CLASS_CAN_BE_USED.warnText()} class Some", false),
            rulesConfigList = rulesConfigListSameVersion
        )
    }

    @Test
    @Tag(WarningNames.INLINE_CLASS_CAN_BE_USED)
    fun `should not trigger on class with internal constructor`() {
        lintMethod(
            """
                |class LocalCommandExecutor internal constructor(private val command: String) {
                |   
                |}
            """.trimMargin(),
            rulesConfigList = rulesConfigListSameVersion
        )
    }

    @Test
    @Tag(WarningNames.INLINE_CLASS_CAN_BE_USED)
    fun `should trigger on class with public constructor`() {
        lintMethod(
            """
                |class LocalCommandExecutor public constructor(private val command: String) {
                |   
                |}
            """.trimMargin(),
            LintError(1, 1, ruleId, "${INLINE_CLASS_CAN_BE_USED.warnText()} class LocalCommandExecutor", false),
            rulesConfigList = rulesConfigListSameVersion
        )
    }

    @Test
    @Tag(WarningNames.INLINE_CLASS_CAN_BE_USED)
    fun `should trigger on class with annotation before the constructor`() {
        lintMethod(
            """
                |class LocalCommandExecutor @Inject constructor(private val command: String) {
                |   
                |}
            """.trimMargin(),
            LintError(1, 1, ruleId, "${INLINE_CLASS_CAN_BE_USED.warnText()} class LocalCommandExecutor", false),
            rulesConfigList = rulesConfigListSameVersion
        )
    }

    @Test
    @Tag(WarningNames.INLINE_CLASS_CAN_BE_USED)
    @Suppress("TOO_LONG_FUNCTION")
    fun `check kotlin version`() {
        lintMethod(
            """
                |class Some {
                |   val config = Config()
                |}
            """.trimMargin(),
            LintError(1, 1, ruleId, "${INLINE_CLASS_CAN_BE_USED.warnText()} class Some", false),
            rulesConfigList = rulesConfigListLateVersion
        )

        lintMethod(
            """
                |class Some {
                |   val config = Config()
                |}
            """.trimMargin(),
            rulesConfigList = rulesConfigListEarlierVersion
        )

        lintMethod(
            """
                |class Some {
                |   val config = Config()
                |}
            """.trimMargin(),
            LintError(1, 1, ruleId, "${INLINE_CLASS_CAN_BE_USED.warnText()} class Some", false),
            rulesConfigList = rulesConfigListSameVersion
        )

        lintMethod(
            """
                |class Some {
                |   val config = Config()
                |}
            """.trimMargin(),
            rulesConfigList = rulesConfigListUnsupportedVersion
        )
    }
}
