import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.4.10"
}

repositories {
    flatDir {
        // to use snapshot diktat without necessary installing
        dirs("../diktat-rules/target")
    }
    mavenCentral()
    jcenter()
}

// default value is needed for correct gradle loading in IDEA; actual value from maven is used during build
val ktlintVersion: String = project.properties.getOrDefault("ktlintVersion", "0.39.0") as String
val diktatVersion = project.version.takeIf { it.toString() != Project.DEFAULT_VERSION } ?: "0.1.6-SNAPSHOT"
dependencies {
    implementation(kotlin("gradle-plugin-api"))

    implementation("com.pinterest.ktlint:ktlint-core:$ktlintVersion") {
        exclude("com.pinterest.ktlint", "ktlint-ruleset-standard")
    }
    implementation("com.pinterest.ktlint:ktlint-reporter-plain:$ktlintVersion")
    implementation("org.cqfn.diktat:diktat-rules:$diktatVersion")
}

val generateVersionsFile by tasks.registering {
    val versionsFile = File("$buildDir/generated/src/generated/Versions.kt")

    outputs.file(versionsFile)

    doFirst {
        versionsFile.parentFile.mkdirs()
        versionsFile.writeText("""
            package generated

            internal const val DIKTAT_VERSION = "$diktatVersion"
            internal const val KTLINT_VERSION = "$ktlintVersion"

            """.trimIndent()
        )
    }
}
sourceSets.main.get().java.srcDir("$buildDir/generated/src")

tasks.withType<KotlinCompile> {
    kotlinOptions {
        // fixme: kotlin 1.3 is required for gradle <6.8
        languageVersion = "1.3"
        apiVersion = "1.3"
    }
    dependsOn.add(generateVersionsFile)
}

gradlePlugin {
    plugins {
        create("diktatPlugin") {
            id = "org.cqfn.diktat.diktat-gradle-plugin"
            implementationClass = "org.cqfn.diktat.plugin.gradle.DiktatGradlePlugin"
        }
    }
}

java {
    withSourcesJar()
}
