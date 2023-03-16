plugins {
    id("org.cqfn.diktat.buildutils.kotlin-jvm-configuration")
    id("org.cqfn.diktat.buildutils.code-quality-convention")
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:1.8.10-1.0.9")
}
