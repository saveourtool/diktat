plugins {
    id("org.cqfn.diktat.buildutils.kotlin-jvm-configuration")
    id("org.cqfn.diktat.buildutils.code-quality-convention")
    id("org.cqfn.diktat.buildutils.publishing-signing-default-configuration")
    alias(libs.plugins.kotlin.plugin.serialization)
}

project.description = "This module builds diktat-api"

dependencies {
    implementation(libs.kotlin.compiler.embeddable)
    implementation(libs.kotlinx.serialization.core)
}

val generateDiktatVersionFile by tasks.registering {
    val outputDir = File("$buildDir/generated/src")
    val versionsFile = outputDir.resolve("generated/DiktatVersion.kt")

    val diktatVersion = version.toString()

    inputs.property("diktat version", diktatVersion)
    outputs.dir(outputDir)

    doFirst {
        versionsFile.parentFile.mkdirs()
        versionsFile.writeText(
            """
            package generated

            const val DIKTAT_VERSION = "$diktatVersion"

            """.trimIndent()
        )
    }
}

kotlin.sourceSets.getByName("main") {
    kotlin.srcDir(
        generateDiktatVersionFile.map {
            it.outputs.files.singleFile
        }
    )
}

tasks.named("diktatFix") {
    dependsOn(
        generateDiktatVersionFile,
        tasks.named("compileKotlin"),
        tasks.named("processResources"),
    )
}
