package com.saveourtool.diktat.ruleset.chapter3.spaces

import org.junit.platform.suite.api.SelectClasses
import org.junit.platform.suite.api.Suite

/**
 * Runs all indentation rule tests.
 */
@Suite
@SelectClasses(
    IndentationRuleWarnTest::class,
    IndentationRuleFixTest::class,
    IndentationRuleTest::class,
)
class IndentationRuleTestSuite
