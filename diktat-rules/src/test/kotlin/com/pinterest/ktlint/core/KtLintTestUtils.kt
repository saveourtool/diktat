/**
 * This class contains util methods for KtLint
 */

package com.pinterest.ktlint.core

typealias LintError = org.cqfn.diktat.api.DiktatError
//
//operator fun LintError.invoke(): LintError = if (canBeAutoCorrected) {
//    DiktatError(line, col, ruleId, detail, true)
//} else {
//    DiktatError(line, col, ruleId, "$detail (cannot be auto-corrected)", false)
//}

//object LintError {
//    operator fun invoke(
//        line: Int,
//        col: Int,
//        ruleId: String,
//        detail: String,
//        canBeAutoCorrected: Boolean = false,
//    ): DiktatError = if (canBeAutoCorrected) {
//        DiktatError(line, col, ruleId, detail, true)
//    } else {
//        DiktatError(line, col, ruleId, "$detail (cannot be auto-corrected)", false)
//    }
//}
