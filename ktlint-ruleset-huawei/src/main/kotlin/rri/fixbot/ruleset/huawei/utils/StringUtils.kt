package rri.fixbot.ruleset.huawei.utils

fun String.isJavaKeyWord() = Keywords.isJavaKeyWord(this)
fun String.isKotlinKeyWord() = Keywords.isKotlinKeyWord(this)

