// ;warn:$line:1: [HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE] files that contain multiple or no classes should contain description of what is inside of this file: there are 0 declared classes and/or objects (cannot be auto-corrected) (diktat-ruleset:header-comment)
package com.saveourtool.diktat

// ;warn:$line:1: [MISSING_KDOC_TOP_LEVEL] all public and internal top-level classes and functions should have Kdoc: foo (cannot be auto-corrected) (diktat-ruleset:kdoc-comments)
// ;warn:$line-1:1: [MISSING_KDOC_ON_FUNCTION] all public, internal and protected functions should have Kdoc with proper tags: foo (cannot be auto-corrected) (diktat-ruleset:kdoc-methods)
fun foo() {
    // ;warn:28: [WRONG_WHITESPACE] incorrect usage of whitespaces for code separation: , should have 0 space(s) before and 1 space(s) after, but has 0 space(s) after (diktat-ruleset:horizontal-whitespace)
    val sum: (Int, Int, Int,) -> Int = fun(
        x,
        y,
        z
    ): Int = x + y + x
    // ;warn:5: [DEBUG_PRINT] use a dedicated logging library: found println() (cannot be auto-corrected) (diktat-ruleset:debug-print)
    println(sum(8, 8, 8))
}

// ;warn:$line:1: [MISSING_KDOC_TOP_LEVEL] all public and internal top-level classes and functions should have Kdoc: boo (cannot be auto-corrected) (diktat-ruleset:kdoc-comments)
// ;warn:$line-1:1: [MISSING_KDOC_ON_FUNCTION] all public, internal and protected functions should have Kdoc with proper tags: boo (cannot be auto-corrected) (diktat-ruleset:kdoc-methods)
fun boo() {
    // ;warn:27: [DEBUG_PRINT] use a dedicated logging library: found println() (cannot be auto-corrected) (diktat-ruleset:debug-print)
    val message = fun() = println("Hello")
}
