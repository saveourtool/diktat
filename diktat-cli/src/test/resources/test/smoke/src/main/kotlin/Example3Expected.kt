// ;warn:$line:1: [HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE] files that contain multiple or no classes should contain description of what is inside of this file: there are 6 declared classes and/or objects (cannot be auto-corrected){{.*}}
package com.saveourtool.diktat

/*
 * Copyright (c) Your Company Name Here. 2010-2020
 */

/**
 * @property name
 */
class HttpClient(var name: String) {
    // ;warn:$line:5: [MISSING_KDOC_CLASS_ELEMENTS] all public, internal and protected classes, functions and variables inside the class should have Kdoc: url (cannot be auto-corrected){{.*}}
    var url: String = ""
    // ;warn:$line:5: [MISSING_KDOC_CLASS_ELEMENTS] all public, internal and protected classes, functions and variables inside the class should have Kdoc: port (cannot be auto-corrected){{.*}}
    var port: String = ""
    // ;warn:$line:5: [MISSING_KDOC_CLASS_ELEMENTS] all public, internal and protected classes, functions and variables inside the class should have Kdoc: timeout (cannot be auto-corrected){{.*}}
    var timeout = 0

    // ;warn:$line:5: [MISSING_KDOC_CLASS_ELEMENTS] all public, internal and protected classes, functions and variables inside the class should have Kdoc: doRequest (cannot be auto-corrected){{.*}}
    // ;warn:$line-1:5: [MISSING_KDOC_ON_FUNCTION] all public, internal and protected functions should have Kdoc with proper tags: doRequest (cannot be auto-corrected){{.*}}
    // ;warn:21: [EMPTY_BLOCK_STRUCTURE_ERROR] incorrect format of empty block: empty blocks are forbidden unless it is function with override keyword (cannot be auto-corrected){{.*}}
    fun doRequest() {}
}

// ;warn:$line:1: [MISSING_KDOC_TOP_LEVEL] all public and internal top-level classes and functions should have Kdoc: Example (cannot be auto-corrected){{.*}}
class Example {
    // ;warn:$line:5: [MISSING_KDOC_CLASS_ELEMENTS] all public, internal and protected classes, functions and variables inside the class should have Kdoc: foo (cannot be auto-corrected){{.*}}
    // ;warn:$line-1:5: [MISSING_KDOC_ON_FUNCTION] all public, internal and protected functions should have Kdoc with proper tags: foo (cannot be auto-corrected){{.*}}
    fun foo() {
        if (condition1 && condition2) {
            bar()
        }

        if (condition3) {
            if (condition4) {
                foo()
            } else {
                bar()
            }
        } else {
            foo()
        }
    }
}

// ;warn:$line:1: [MISSING_KDOC_TOP_LEVEL] all public and internal top-level classes and functions should have Kdoc: IssueType (cannot be auto-corrected){{.*}}
enum class IssueType {
    PROJECT_STRUCTURE, TESTS, VCS
}

// ;warn:$line:1: [MISSING_KDOC_TOP_LEVEL] all public and internal top-level classes and functions should have Kdoc: IssueType2 (cannot be auto-corrected){{.*}}
enum class IssueType2 {
    PROJECT_STRUCTURE,
    TESTS,
    VCS,
    ;

    /**
     * @param bar
     // ;warn:8: [KDOC_NO_EMPTY_TAGS] no empty descriptions in tag blocks are allowed: @return (cannot be auto-corrected){{.*}}
     * @return
     */
    fun foo(bar: Int) = bar

    companion object
}

// ;warn:$line:1: [MISSING_KDOC_TOP_LEVEL] all public and internal top-level classes and functions should have Kdoc: IssueType3 (cannot be auto-corrected){{.*}}
enum class IssueType3 {
    // ;warn:5: [IDENTIFIER_LENGTH] identifier's length is incorrect, it should be in range of [2, 64] symbols: A (cannot be auto-corrected){{.*}}
    A,
    // ;warn:5: [CONFUSING_IDENTIFIER_NAMING] it's a bad name for identifier: better name is: bt, nxt (cannot be auto-corrected){{.*}}
    // ;warn:5: [IDENTIFIER_LENGTH] identifier's length is incorrect, it should be in range of [2, 64] symbols: B (cannot be auto-corrected){{.*}}
    B,
    // ;warn:5: [IDENTIFIER_LENGTH] identifier's length is incorrect, it should be in range of [2, 64] symbols: C (cannot be auto-corrected){{.*}}
    C,
    // ;warn:5: [CONFUSING_IDENTIFIER_NAMING] it's a bad name for identifier: better name is: obj, dgt (cannot be auto-corrected){{.*}}
    // ;warn:5: [IDENTIFIER_LENGTH] identifier's length is incorrect, it should be in range of [2, 64] symbols: D (cannot be auto-corrected){{.*}}
    D,
    ;
}

// ;warn:$line:1: [MISSING_KDOC_TOP_LEVEL] all public and internal top-level classes and functions should have Kdoc: Foo (cannot be auto-corrected){{.*}}
// ;warn:$line+4:8: [KDOC_NO_NEWLINE_AFTER_SPECIAL_TAGS] in KDoc there should be exactly one empty line after special tags: @implNote{{.*}}
// ;warn:$line+5:23: [EMPTY_BLOCK_STRUCTURE_ERROR] incorrect format of empty block: empty blocks are forbidden unless it is function with override keyword (cannot be auto-corrected){{.*}}
class Foo {
    /**
     * @implNote lorem ipsum
     */
    private fun foo() {}
}

// ;warn:$line:1: [MISSING_KDOC_TOP_LEVEL] all public and internal top-level classes and functions should have Kdoc: mains (cannot be auto-corrected){{.*}}
// ;warn:$line-1:1: [MISSING_KDOC_ON_FUNCTION] all public, internal and protected functions should have Kdoc with proper tags: mains (cannot be auto-corrected){{.*}}
fun mains() {
    val httpClient = HttpClient("myConnection")
        .apply {
            url = "http://example.com"
            port = "8080"
            // ;warn:23: [MAGIC_NUMBER] avoid using magic numbers, instead define constants with clear names describing what the magic number means: 100 (cannot be auto-corrected){{.*}}
            timeout = 100
        }
    httpClient.doRequest()
}
