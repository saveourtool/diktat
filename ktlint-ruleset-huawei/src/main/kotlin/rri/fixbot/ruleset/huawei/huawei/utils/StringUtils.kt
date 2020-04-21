package rri.fixbot.ruleset.huawei.huawei.utils

fun String.isJavaKeyWord() = Keywords.isJavaKeyWord(this)
fun String.isKotlinKeyWord() = Keywords.isKotlinKeyWord(this)

