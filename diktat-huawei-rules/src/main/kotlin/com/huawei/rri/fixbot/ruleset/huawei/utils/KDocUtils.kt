package com.huawei.rri.fixbot.ruleset.huawei.utils

import com.pinterest.ktlint.core.ast.ElementType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.kdoc.parser.KDocKnownTag
import org.jetbrains.kotlin.kdoc.psi.impl.KDocTag

fun ASTNode.kDocTags(): Collection<KDocTag>? {
    val kDoc = this.getFirstChildWithType(ElementType.KDOC)
    return kDoc?.getFirstChildWithType(ElementType.KDOC_SECTION)
        ?.getAllChildrenWithType(ElementType.KDOC_TAG)?.map { it.psi as KDocTag }
}

fun Iterable<KDocTag>.hasKnownKDocTag(knownTag: KDocKnownTag): Boolean =
    this.find { it.knownTag == knownTag } != null
