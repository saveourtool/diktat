// ;warn:$line:1: [FILE_NAME_MATCH_CLASS] file name is incorrect - it should match with the class described in it if there is the only one class declared: Example5Expected.kt vs Some{{.*}}
/*
    Copyright 2018-2024 John Doe.
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0
*/

package com.saveourtool.diktat

// ;warn:$line:1: [MISSING_KDOC_TOP_LEVEL] all public and internal top-level classes and functions should have Kdoc: Some (cannot be auto-corrected){{.*}}
// ;warn:$line-1:1: [USE_DATA_CLASS] this class can be converted to a data class: Some (cannot be auto-corrected){{.*}}
class Some {
    // ;warn:$line:5: [MISSING_KDOC_CLASS_ELEMENTS] all public, internal and protected classes, functions and variables inside the class should have Kdoc: config (cannot be auto-corrected){{.*}}
    val config = Config()
}
