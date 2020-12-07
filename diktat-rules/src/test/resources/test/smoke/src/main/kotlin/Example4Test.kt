package org.cqfn.diktat

fun bar() {
    val diktatExtension = project.extensions.create(DIKTAT_EXTENSION, DiktatExtension::class.java)
    diktatExtension.inputs = project.fileTree("src").apply {
        include("**/*.kt")
    }
    diktatExtension.reporter = PlainReporter(System.out)
}

