package org.cqfn.diktat.ruleset.chapter5

import org.cqfn.diktat.ruleset.constants.Warnings.RUN_BLOCKING_INSIDE_ASYNC
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.chapter5.AsyncAndSyncRule
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class AsyncAndSyncRuleTest : LintTestBase(::AsyncAndSyncRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${AsyncAndSyncRule.nameId}"

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
            LintError(11, 8, ruleId, "${RUN_BLOCKING_INSIDE_ASYNC.warnText()} runBlocking", false),
            LintError(18, 4, ruleId, "${RUN_BLOCKING_INSIDE_ASYNC.warnText()} runBlocking", false)
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
            LintError(4, 8, ruleId, "${RUN_BLOCKING_INSIDE_ASYNC.warnText()} runBlocking", false)
        )
    }
}
