// ;warn:$line:1: [HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE] files that contain multiple or no classes should contain description of what is inside of this file: there are 0 declared classes and/or objects (cannot be auto-corrected) (diktat-ruleset:header-comment)
package com.saveourtool.diktat.script

run {
    // ;warn:5: [DEBUG_PRINT] use a dedicated logging library: found println() (cannot be auto-corrected) (diktat-ruleset:debug-print)
    println("hello world!")
}

// ;warn:$line:1: [MISSING_KDOC_ON_FUNCTION] all public, internal and protected functions should have Kdoc with proper tags: foo (cannot be auto-corrected) (diktat-ruleset:kdoc-methods)
fun foo() {
    // ;warn:5: [DEBUG_PRINT] use a dedicated logging library: found println() (cannot be auto-corrected) (diktat-ruleset:debug-print)
    println()
}

// ;warn:5: [IDENTIFIER_LENGTH] identifier's length is incorrect, it should be in range of [2, 64] symbols: q (cannot be auto-corrected) (diktat-ruleset:identifier-naming)
val q = Config()

run {
    // ;warn:5: [DEBUG_PRINT] use a dedicated logging library: found println() (cannot be auto-corrected) (diktat-ruleset:debug-print)
    println("a")
}

also {
    // ;warn:5: [DEBUG_PRINT] use a dedicated logging library: found println() (cannot be auto-corrected) (diktat-ruleset:debug-print)
    println("a")
}
