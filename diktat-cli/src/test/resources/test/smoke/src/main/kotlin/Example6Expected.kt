// ;warn:$line:1: [HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE] files that contain multiple or no classes should contain description of what is inside of this file: there are 0 declared classes and/or objects (cannot be auto-corrected){{.*}}
package com.saveourtool.diktat

val foo =
        """
            some
            cool
            text
        """.trimIndent()

val bar =
        """
            | some
            | text
        """.trimMargin()

val text =
        """
       x
    """

val dockerFileAsText =
        """
            FROM $baseImage someTest
            COPY resources $resourcesPath
            RUN /bin/bash
        """.trimIndent()  // RUN command shouldn't matter because it will be replaced on container creation
