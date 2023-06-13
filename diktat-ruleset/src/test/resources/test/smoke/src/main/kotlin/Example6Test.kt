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
