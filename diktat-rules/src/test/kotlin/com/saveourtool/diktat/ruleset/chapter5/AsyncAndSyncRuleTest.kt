package com.saveourtool.diktat.ruleset.chapter5

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings.RUN_BLOCKING_INSIDE_ASYNC
import com.saveourtool.diktat.ruleset.rules.chapter5.AsyncAndSyncRule
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class AsyncAndSyncRuleTest : LintTestBase(::AsyncAndSyncRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${AsyncAndSyncRule.NAME_ID}"

    @Test
    @Tag(WarningNames.RUN_BLOCKING_INSIDE_ASYNC)
    fun `test wrong case`() {
        lintMethod(
            """
                    |fun foo() {
                    |   GlobalScope.launch {
                    |       c.addAndGet(i)
                    |   }
                    |
                    |   GlobalScope.async {
                    |       n
                    |   }
                    |
                    |   GlobalScope.async {
                    |       runBlocking {
                    |
                    |       }
                    |   }
                    |}
                    |
                    |suspend fun foo() {
                    |   runBlocking {
                    |       delay(2000)
                    |   }
                    |}
                    |
            """.trimMargin(),
            DiktatError(11, 8, ruleId, "${RUN_BLOCKING_INSIDE_ASYNC.warnText()} runBlocking", false),
            DiktatError(18, 4, ruleId, "${RUN_BLOCKING_INSIDE_ASYNC.warnText()} runBlocking", false)
        )
    }

    @Test
    @Tag(WarningNames.RUN_BLOCKING_INSIDE_ASYNC)
    fun `test dot qualified expression case`() {
        lintMethod(
            """
                    |fun foo() {
                    |   GlobalScope.async {
                    |       node.runBlocking()
                    |       runBlocking {
                    |           n++
                    |       }
                    |   }
                    |}
                    |
                    |fun goo() {
                    |   runBlocking {
                    |       GlobalScope.async {
                    |           n++
                    |       }
                    |   }
                    |}
            """.trimMargin(),
            DiktatError(4, 8, ruleId, "${RUN_BLOCKING_INSIDE_ASYNC.warnText()} runBlocking", false)
        )
    }
}
