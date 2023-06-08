plugins {
    id("org.cqfn.diktat.buildutils.kotlin-jvm-configuration")
    id("org.cqfn.diktat.buildutils.code-quality-convention")
}

dependencies {
    implementation(libs.kotlin.ksp.api)
}

tasks.named("diktatFix") {
    dependsOn(
        tasks.named("compileKotlin"),
        tasks.named("processResources"),
    )
}
