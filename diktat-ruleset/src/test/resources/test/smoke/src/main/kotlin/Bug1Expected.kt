// ;warn:$line:1: [FILE_NAME_MATCH_CLASS] file name is incorrect - it should match with the class described in it if there is the only one class declared: Bug1Expected.kt vs D{{.*}}
package com.saveourtool.diktat

// ;warn:$line:1: [MISSING_KDOC_TOP_LEVEL] all public and internal top-level classes and functions should have Kdoc: D (cannot be auto-corrected){{.*}}
// ;warn:7: [CLASS_NAME_INCORRECT] class/enum/interface name should be in PascalCase and should contain only latin (ASCII) letters or numbers: D{{.*}}
// ;warn:7: [IDENTIFIER_LENGTH] identifier's length is incorrect, it should be in range of [2, 64] symbols: D (cannot be auto-corrected){{.*}}
class D {
    // ;warn:$line:5: [MISSING_KDOC_CLASS_ELEMENTS] all public, internal and protected classes, functions and variables inside the class should have Kdoc: x (cannot be auto-corrected){{.*}}
    val x = 0

    /**
     // ;warn:8: [KDOC_NO_EMPTY_TAGS] no empty descriptions in tag blocks are allowed: @return (cannot be auto-corrected){{.*}}
     * @return
     */
    fun bar(): Bar {
        // ;warn:19: [MAGIC_NUMBER] avoid using magic numbers, instead define constants with clear names describing what the magic number means: 42 (cannot be auto-corrected){{.*}}
        val qux = 42
        return Bar(qux)
    }
}

/**
 * @param foo
 */
fun readFile(foo: Foo) {
    var bar: Bar
}
