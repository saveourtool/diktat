package org.cqfn.diktat.ruleset.chapter3.spaces

import org.junit.platform.suite.api.SelectClasses
import org.junit.platform.suite.api.Suite

@Suite
@SelectClasses(
    IndentationRuleWarnTest::class,
    IndentationRuleFixTest::class)
class IndentationRuleTest
