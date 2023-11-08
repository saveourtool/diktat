// ;warn:$line:1: [FILE_NAME_MATCH_CLASS] file name is incorrect - it should match with the class described in it if there is the only one class declared: Example1Expected.kt vs Example{{.*}}
package com.saveourtool.diktat

// ;warn:$line:1: [MISSING_KDOC_TOP_LEVEL] all public and internal top-level classes and functions should have Kdoc: Example (cannot be auto-corrected){{.*}}
class Example {
    // ;warn:$line:5: [MISSING_KDOC_CLASS_ELEMENTS] all public, internal and protected classes, functions and variables inside the class should have Kdoc: isValid (cannot be auto-corrected){{.*}}
    @get:JvmName("getIsValid")
    val isValid = true
    // ;warn:$line:5: [MISSING_KDOC_CLASS_ELEMENTS] all public, internal and protected classes, functions and variables inside the class should have Kdoc: foo (cannot be auto-corrected){{.*}}
    val foo: Int = 1

    // ;warn:$line:5: [MISSING_KDOC_CLASS_ELEMENTS] all public, internal and protected classes, functions and variables inside the class should have Kdoc: foo (cannot be auto-corrected){{.*}}
    // ;warn:$line-1:5: [MISSING_KDOC_ON_FUNCTION] all public, internal and protected functions should have Kdoc with proper tags: foo (cannot be auto-corrected){{.*}}
    // ;warn:19: [EMPTY_BLOCK_STRUCTURE_ERROR] incorrect format of empty block: empty blocks are forbidden unless it is function with override keyword (cannot be auto-corrected){{.*}}
    fun Foo.foo() { }

    /**
     * @param x
     * @param y
    // ;warn:8: [KDOC_NO_EMPTY_TAGS] no empty descriptions in tag blocks are allowed: @return (cannot be auto-corrected){{.*}}
     * @return
     */
    fun bar(x: Int, y: Int): Int = x + y

    /**
     * @param sub
    // ;warn:8: [KDOC_NO_EMPTY_TAGS] no empty descriptions in tag blocks are allowed: @return (cannot be auto-corrected){{.*}}
     * @return
     */
    fun String.countSubStringOccurrences(sub: String): Int {
        // println("sub: $sub")
        return this.split(sub).size - 1
    }

    /**
    // ;warn:8: [KDOC_NO_EMPTY_TAGS] no empty descriptions in tag blocks are allowed: @return (cannot be auto-corrected){{.*}}
     * @return
     */
    fun String.splitPathToDirs(): List<String> =
            this.replace("\\", "/").replace("//", "/")
                .split("/")

    /**
     * @param x
     * @param y
    // ;warn:8: [KDOC_NO_EMPTY_TAGS] no empty descriptions in tag blocks are allowed: @return (cannot be auto-corrected){{.*}}
     * @return
     */
    fun foo(x: Int,
            y: Int): Int = x +
            (y +
                    bar(x, y)
            )
}
