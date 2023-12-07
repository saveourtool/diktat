package com.saveourtool.diktat.ruleset.config

/**
 * Configuration that allows customizing additional options of particular rules.
 * @property config a map of strings with configuration options for a particular rule
 */
open class RuleConfiguration(protected val config: Map<String, String>)
