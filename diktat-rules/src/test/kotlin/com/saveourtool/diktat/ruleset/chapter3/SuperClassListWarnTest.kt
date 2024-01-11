package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings.WRONG_NEWLINES
import com.saveourtool.diktat.ruleset.rules.chapter3.files.NewlinesRule
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import com.saveourtool.diktat.ruleset.constants.Warnings
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class SuperClassListWarnTest : LintTestBase(::NewlinesRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${NewlinesRule.NAME_ID}"

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `superclass list on the same line`() {
        lintMethod(
            """
                    |package com.saveourtool.diktat
                    |
                    |class A<K : Any, P : Any, G : Any> : B<K>(), C<P>, D<G> {}
            """.trimMargin(),
            DiktatError(3, 38, ruleId, "${Warnings.WRONG_NEWLINES.warnText()} supertype list entries should be placed on different lines in declaration of <A>", true),
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `first superclass also on a new line`() {
        lintMethod(
            """
                    |package com.saveourtool.diktat
                    |
                    |class A<K : Any, P : Any, G : Any> : B<K>(),
                    |C<P>,
                    |D<G> {}
            """.trimMargin(),
            DiktatError(3, 38, ruleId, "${Warnings.WRONG_NEWLINES.warnText()} supertype list entries should be placed on different lines in declaration of <A>", true),
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `superclass list of 2 elements on the same line`() {
        lintMethod(
            """
                    |package com.saveourtool.diktat
                    |
                    |class A<K : Any, P : Any, G : Any> : B<K>(), C<P> {}
            """.trimMargin(),
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `superclass list on separate lines`() {
        lintMethod(
            """
                    |package com.saveourtool.diktat
                    |
                    |class A<K : Any, P : Any, G : Any> :
                    |B<K>(),
                    |C<P>,
                    |D<G> {}
            """.trimMargin(),
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `superclass list different whitespaces`() {
        lintMethod(
            """
                    |package com.saveourtool.diktat
                    |
                    |class A<K : Any, P : Any, G : Any> :
                    |B<K>(),
                    |    C<P>, D<G> {}
            """.trimMargin(),
            DiktatError(4, 1, ruleId, "${Warnings.WRONG_NEWLINES.warnText()} supertype list entries should be placed on different lines in declaration of <A>", true),
            )
    }
}
