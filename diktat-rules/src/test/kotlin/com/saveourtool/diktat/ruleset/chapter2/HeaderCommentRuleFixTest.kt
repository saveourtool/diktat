package com.saveourtool.diktat.ruleset.chapter2

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.HEADER_MISSING_OR_WRONG_COPYRIGHT
import com.saveourtool.diktat.ruleset.constants.Warnings.HEADER_WRONG_FORMAT
import com.saveourtool.diktat.ruleset.rules.chapter2.comments.HeaderCommentRule
import com.saveourtool.diktat.test.framework.processing.ResourceReader.Companion.withReplacements
import com.saveourtool.diktat.util.FixTestBase

import generated.WarningNames
import generated.WarningNames.WRONG_COPYRIGHT_YEAR
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.time.LocalDate

class HeaderCommentRuleFixTest : FixTestBase(
    "test/paragraph2/header",
    ::HeaderCommentRule,
    listOf(
        RulesConfig("HEADER_MISSING_OR_WRONG_COPYRIGHT", true,
            mapOf(
                "isCopyrightMandatory" to "true",
                "copyrightText" to "Copyright (c) Huawei Technologies Co., Ltd. 2020-$currentYear. All rights reserved.")
        ),
        RulesConfig("HEADER_WRONG_FORMAT", true, emptyMap())
    )
) {
    @Test
    @Tag(WarningNames.HEADER_WRONG_FORMAT)
    fun `new line should be inserted after header KDoc`(@TempDir tempDir: Path) {
        fixAndCompare("NewlineAfterHeaderKdocExpected.kt", "NewlineAfterHeaderKdocTest.kt", overrideResourceReader = { it.withReplacements(tempDir, currentYearReplacement) })
    }

    @Test
    @Tag(WarningNames.HEADER_MISSING_OR_WRONG_COPYRIGHT)
    fun `if no copyright is present and mandatoryCopyright=true, it is added`(@TempDir tempDir: Path) {
        fixAndCompare("AutoCopyrightExpected.kt", "AutoCopyrightTest.kt", overrideResourceReader = { it.withReplacements(tempDir, currentYearReplacement) })
    }

    @Test
    @Tag(WarningNames.HEADER_MISSING_OR_WRONG_COPYRIGHT)
    fun `if no copyright is present, added it and apply pattern for current year`(@TempDir tempDir: Path) {
        fixAndCompare("AutoCopyrightApplyPatternExpected.kt", "AutoCopyrightApplyPatternTest.kt",
            listOf(
                RulesConfig(
                    HEADER_MISSING_OR_WRONG_COPYRIGHT.name, true,
                    mapOf(
                        "isCopyrightMandatory" to "true",
                        "copyrightText" to "Copyright (c) Huawei Technologies Co., Ltd. 2020-;@currYear;. All rights reserved."
                    )
                ),
                RulesConfig(HEADER_WRONG_FORMAT.name, true, emptyMap())
            ),
            overrideResourceReader = { it.withReplacements(tempDir, currentYearReplacement) },
        )
    }

    /**
     * Fixme there shouldn't be an additional blank line after copyright
     */
    @Test
    @Tag(WarningNames.HEADER_NOT_BEFORE_PACKAGE)
    fun `header KDoc should be moved before package`(@TempDir tempDir: Path) {
        fixAndCompare("MisplacedHeaderKdocExpected.kt", "MisplacedHeaderKdocTest.kt", overrideResourceReader = { it.withReplacements(tempDir, currentYearReplacement) })
    }

    @Test
    @Tags(Tag(WarningNames.HEADER_MISSING_OR_WRONG_COPYRIGHT), Tag(WarningNames.HEADER_WRONG_FORMAT))
    fun `header KDoc should be moved before package - no copyright`(@TempDir tempDir: Path) {
        fixAndCompare("MisplacedHeaderKdocNoCopyrightExpected.kt", "MisplacedHeaderKdocNoCopyrightTest.kt",
            listOf(RulesConfig(HEADER_MISSING_OR_WRONG_COPYRIGHT.name, false, emptyMap()), RulesConfig(HEADER_WRONG_FORMAT.name, true, emptyMap())),
            overrideResourceReader = { it.withReplacements(tempDir, currentYearReplacement) },
        )
    }

    @Test
    @Tags(Tag(WarningNames.HEADER_NOT_BEFORE_PACKAGE), Tag(WarningNames.HEADER_MISSING_OR_WRONG_COPYRIGHT))
    fun `header KDoc should be moved before package - appended copyright`(@TempDir tempDir: Path) {
        fixAndCompare(
            "MisplacedHeaderKdocAppendedCopyrightExpected.kt",
            "MisplacedHeaderKdocAppendedCopyrightTest.kt",
            overrideResourceReader = { it.withReplacements(tempDir, currentYearReplacement) },
        )
    }

    @Test
    @Tag(WRONG_COPYRIGHT_YEAR)
    fun `copyright invalid year should be auto-corrected`(@TempDir tempDir: Path) {
        fixAndCompare("CopyrightDifferentYearExpected.kt", "CopyrightDifferentYearTest.kt",
            listOf(RulesConfig(HEADER_MISSING_OR_WRONG_COPYRIGHT.name, true, mapOf(
                "isCopyrightMandatory" to "true",
                "copyrightText" to "Copyright (c) My Company., Ltd. 2012-2019. All rights reserved."
            ))),
            overrideResourceReader = { it.withReplacements(tempDir, currentYearReplacement) },
        )
    }

    @Test
    @Tag(WRONG_COPYRIGHT_YEAR)
    fun `copyright invalid year should be auto-corrected 2`(@TempDir tempDir: Path) {
        fixAndCompare("CopyrightDifferentYearExpected2.kt", "CopyrightDifferentYearTest2.kt",
            listOf(RulesConfig(HEADER_MISSING_OR_WRONG_COPYRIGHT.name, true, mapOf(
                "isCopyrightMandatory" to "true",
                "copyrightText" to "Copyright (c) My Company., Ltd. 2021. All rights reserved."
            ))),
            overrideResourceReader = { it.withReplacements(tempDir, currentYearReplacement) },
        )
    }

    @Test
    @Tag(WRONG_COPYRIGHT_YEAR)
    fun `copyright invalid pattern, but valid in code`(@TempDir tempDir: Path) {
        fixAndCompare("CopyrightInvalidPatternValidCodeExpected.kt", "CopyrightInvalidPatternValidCodeTest.kt",
            listOf(RulesConfig(HEADER_MISSING_OR_WRONG_COPYRIGHT.name, true, mapOf(
                "isCopyrightMandatory" to "true",
                "copyrightText" to "Copyright (c) My Company., Ltd. 2012-2019. All rights reserved."
            ))),
            overrideResourceReader = { it.withReplacements(tempDir, currentYearReplacement) },
        )
    }

    @Test
    @Tag(WRONG_COPYRIGHT_YEAR)
    fun `copyright invalid pattern, update actual year in it and auto-correct`(@TempDir tempDir: Path) {
        fixAndCompare("CopyrightAbsentInvalidPatternExpected.kt", "CopyrightAbsentInvalidPatternTest.kt",
            listOf(RulesConfig(HEADER_MISSING_OR_WRONG_COPYRIGHT.name, true, mapOf(
                "isCopyrightMandatory" to "true",
                "copyrightText" to "Copyright (c) My Company., Ltd. 2012-2019. All rights reserved."
            ))),
            overrideResourceReader = { it.withReplacements(tempDir, currentYearReplacement) },
        )
    }

    @Test
    @Tag(WRONG_COPYRIGHT_YEAR)
    fun `should not raise npe`(@TempDir tempDir: Path) {
        fixAndCompare("CopyrightShouldNotTriggerNPEExpected.kt", "CopyrightShouldNotTriggerNPETest.kt",
            listOf(RulesConfig(HEADER_MISSING_OR_WRONG_COPYRIGHT.name, true, mapOf(
                "isCopyrightMandatory" to "true",
                "copyrightText" to "Copyright (c) My Company., Ltd. 2012-2021. All rights reserved."
            ))),
            overrideResourceReader = { it.withReplacements(tempDir, currentYearReplacement) },
        )
    }

    @Test
    @Tag(WarningNames.HEADER_MISSING_OR_WRONG_COPYRIGHT)
    fun `copyright multiline`(@TempDir tempDir: Path) {
        fixAndCompare("MultilineCopyrightExample.kt", "MultilineCopyrightTest.kt",
            listOf(RulesConfig(HEADER_MISSING_OR_WRONG_COPYRIGHT.name, true, mapOf(
                "isCopyrightMandatory" to "true",
                "copyrightText" to """
                |    Copyright 2018-$currentYear John Doe.
                |
                |    Licensed under the Apache License, Version 2.0 (the "License");
                |    you may not use this file except in compliance with the License.
                |    You may obtain a copy of the License at
                |
                |        http://www.apache.org/licenses/LICENSE-2.0
                |
                |    Unless required by applicable law or agreed to in writing, software
                |    distributed under the License is distributed on an "AS IS" BASIS,
                |    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
                |    See the License for the specific language governing permissions and
                |    limitations under the License.
                """.trimMargin()
            ))),
            overrideResourceReader = { it.withReplacements(tempDir, currentYearReplacement) },
        )
    }

    @Test
    @Tag(WarningNames.HEADER_MISSING_OR_WRONG_COPYRIGHT)
    fun `should not trigger if copyright text have different indents`(@TempDir tempDir: Path) {
        fixAndCompare("MultilineCopyrightNotTriggerExample.kt", "MultilineCopyrightNotTriggerTest.kt",
            listOf(RulesConfig(HEADER_MISSING_OR_WRONG_COPYRIGHT.name, true, mapOf(
                "isCopyrightMandatory" to "true",
                "copyrightText" to """
                    |   Copyright 2018-2020 John Doe.
                    |
                    |   Licensed under the Apache License, Version 2.0 (the "License");
                    |   you may not use this file except in compliance with the License.
                    |   You may obtain a copy of the License at
            """.trimMargin()
            ))),
            overrideResourceReader = { it.withReplacements(tempDir, currentYearReplacement) },
        )
    }

    companion object {
        private const val PLACEHOLDER = "%%YEAR%%"
        private val currentYear = LocalDate.now().year.toString()
        private val currentYearReplacement = mapOf(PLACEHOLDER to currentYear)
    }
}
