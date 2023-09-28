plugins {
    id("com.saveourtool.diktat.buildutils.kotlin-jvm-configuration")
    id("com.saveourtool.diktat.buildutils.code-quality-convention")
    id("com.saveourtool.diktat.buildutils.publishing-default-configuration")
    alias(libs.plugins.kotlin.plugin.serialization)
}

project.description = "This module builds diktat-api"

dependencies {
    implementation(libs.kotlin.compiler.embeddable)
    implementation(libs.kotlinx.serialization.core)
}

val generateDiktatVersionFile by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/src").get().asFile
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

sequenceOf("diktatFix", "diktatCheck").forEach { diktatTaskName ->
    tasks.findByName(diktatTaskName)?.dependsOn(
        generateDiktatVersionFile,
        tasks.named("compileKotlin"),
        tasks.named("processResources"),
    )
}
