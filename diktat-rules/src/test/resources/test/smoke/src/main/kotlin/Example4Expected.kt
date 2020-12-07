package org.cqfn.diktat

fun bar() {
    val diktatExtension = project.extensions.create(DIKTAT_EXTENSION, DiktatExtension::class.java).apply {
        inputs = project.fileTree("src").apply {
            include("**/*.kt")
        }
        reporter = PlainReporter(System.out)}
}

