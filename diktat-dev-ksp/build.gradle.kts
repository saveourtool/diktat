plugins {
    id("com.saveourtool.diktat.buildutils.kotlin-jvm-configuration")
    id("com.saveourtool.diktat.buildutils.code-quality-convention")
}

dependencies {
    implementation(libs.kotlin.ksp.api)
}

sequenceOf("diktatFix", "diktatCheck").forEach { diktatTaskName ->
    tasks.findByName(diktatTaskName)?.dependsOn(
        tasks.named("compileKotlin"),
        tasks.named("processResources"),
    )
}
