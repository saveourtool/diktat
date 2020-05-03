package             com.huawei.ktlint.ruleset.a_

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl

/**
 * Alphabetical with capital letters before lower case letters (e.g. Z before a).
 * No blank lines between major groups (android, com, junit, net, org, java, javax).
 * Single group regardless of import type.
 *
 * https://developer.android.com/kotlin/style-guide#import_statements
 */
class TestPackageName {

}
