plugins {
    id("org.cqfn.diktat.diktat-gradle-plugin") version "0.1.5"
}

diktat {
    inputs = files("diktat-rules/src/test/resources/test/funcTest/FunctionalTestFile.kt")
    debug = true  // fixme: remove after #544
}