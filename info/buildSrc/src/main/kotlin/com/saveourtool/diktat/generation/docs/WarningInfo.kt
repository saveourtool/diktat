package com.saveourtool.diktat.generation.docs

interface WarningInfo {
    val ruleId: String
    val name: String
    val canBeAutoCorrected: Boolean
    fun getChapterByWarning(): CharterInfo
}
