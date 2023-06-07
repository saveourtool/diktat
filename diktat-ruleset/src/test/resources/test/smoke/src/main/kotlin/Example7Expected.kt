// ;warn:$line:1: [HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE] files that contain multiple or no classes should contain description of what is inside of this file: there are 0 declared classes and/or objects (cannot be auto-corrected){{.*}}
package com.saveourtool.diktat

// ;warn:$line:1: [MISSING_KDOC_TOP_LEVEL] all public and internal top-level classes and functions should have Kdoc: foo (cannot be auto-corrected){{.*}}
// ;warn:$line-1:1: [MISSING_KDOC_ON_FUNCTION] all public, internal and protected functions should have Kdoc with proper tags: foo (cannot be auto-corrected){{.*}}
fun foo() {
    val prop: Int = 0

    prop ?: run {
        println("prop is null")
        bar()
    }

    prop?.let {
        baz()
        gaz()
    }

    prop?.let {
        doAnotherSmth()
    } ?: doSmth()
}

// ;warn:$line:1: [MISSING_KDOC_TOP_LEVEL] all public and internal top-level classes and functions should have Kdoc: fooo (cannot be auto-corrected){{.*}}
// ;warn:$line-1:1: [MISSING_KDOC_ON_FUNCTION] all public, internal and protected functions should have Kdoc with proper tags: fooo (cannot be auto-corrected){{.*}}
fun fooo() {
    if (a) {
        bar()
    } else b?.let {
        baz()
    }
        ?: run {
            qux()
        }
}
