package com.saveourtool.diktat.ruleset.junit

/**
 * @property valueOrNull a boolean value, or `null` (meaning the default value
 *   will be used).
 */
@Suppress("WRONG_DECLARATIONS_ORDER")
enum class BooleanOrDefault(val valueOrNull: Boolean?) {
    FALSE(false),
    TRUE(true),
    DEFAULT(null),
    ;
}
