// ;warn:$line:1: [HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE] files that contain multiple or no classes should contain description of what is inside of this file: there are 0 declared classes and/or objects (cannot be auto-corrected) (diktat-ruleset:header-comment)
package com.saveourtool.diktat

/**
 * @param bar lorem ipsum
 *
 * dolor sit amet
// ;warn:4: [KDOC_NO_EMPTY_TAGS] no empty descriptions in tag blocks are allowed: @return (cannot be auto-corrected) (diktat-ruleset:kdoc-formatting)
 * @return
 */
fun foo1(bar: Bar): Baz {
    // placeholder
}

/**
 * @param bar lorem ipsum
 *
 * dolor sit amet
// ;warn:4: [KDOC_NO_EMPTY_TAGS] no empty descriptions in tag blocks are allowed: @return (cannot be auto-corrected) (diktat-ruleset:kdoc-formatting)
 * @return
 */
fun foo2(bar: Bar): Baz {
    // placeholder
}
