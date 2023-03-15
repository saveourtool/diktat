import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentOperatingSystem

plugins {
    `java-gradle-plugin`
    kotlin("jvm")
    jacoco
    id("pl.droidsonroids.jacoco.testkit") version "1.0.9"
    id("org.gradle.test-retry") version "1.5.2"
}

dependencies {
    implementation(kotlin("gradle-plugin-api"))
    implementation(libs.sarif4k.jvm)

    api(projects.diktatCommon) {
        exclude("org.jetbrains.kotlin", "kotlin-compiler-embeddable")
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk7")
        exclude("org.jetbrains.kotlin", "kotlin-stdlib")
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")
        exclude("org.slf4j", "slf4j-log4j12")
    }

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)

    // FIXME: it should come as transitive dependency from projects.diktatCommon
    implementation(libs.kotlinx.serialization.json)
}

val generateVersionsFile by tasks.registering {
    val versionsFile = File("$buildDir/generated/src/generated/Versions.kt")
    val diktatVersion = project.version.toString()
    val ktlintVersion = libs.versions.ktlint.get()

    inputs.property("diktat version", diktatVersion)
    inputs.property("ktlint version", ktlintVersion)

    outputs.file(versionsFile)

    doFirst {
        versionsFile.parentFile.mkdirs()
        versionsFile.writeText(
            """
            package generated

            internal const val DIKTAT_VERSION = "$diktatVersion"
            internal const val KTLINT_VERSION = "$ktlintVersion"

            """.trimIndent()
        )
    }
}
kotlin.sourceSets["main"].kotlin.srcDir("$buildDir/generated/src")

tasks.withType<KotlinCompile> {
    kotlinOptions {
        // fixme: kotlin 1.3 is required for gradle <6.8
        languageVersion = "1.3"
        apiVersion = "1.3"
        jvmTarget = "1.8"
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

// === testing & code coverage, jacoco is run independent from maven
val functionalTestTask by tasks.register<Test>("functionalTest")
tasks.withType<Test> {
    useJUnitPlatform()
}
jacoco.toolVersion = libs.versions.jacoco.get()

// === integration testing
// fixme: should probably use KotlinSourceSet instead
val functionalTest = sourceSets.create("functionalTest") {
    compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath.get()
    runtimeClasspath += output + compileClasspath
}
tasks.getByName<Test>("functionalTest") {
    shouldRunAfter("test")
    testClassesDirs = functionalTest.output.classesDirs
    classpath = functionalTest.runtimeClasspath
    maxParallelForks = Runtime.getRuntime().availableProcessors()
    maxHeapSize = "1024m"
    retry {
        failOnPassedAfterRetry.set(false)
        maxFailures.set(10)
        maxRetries.set(3)
    }
    doLast {
        if (getCurrentOperatingSystem().isWindows) {
            // workaround for https://github.com/koral--/jacoco-gradle-testkit-plugin/issues/9
            logger.lifecycle("Sleeping for 5 sec after functionalTest to avoid error with file locking")
            Thread.sleep(5_000)
        }
    }
    finalizedBy(tasks.jacocoTestReport)
}
tasks.check { dependsOn(tasks.jacocoTestReport) }
jacocoTestKit {
    applyTo("functionalTestRuntimeOnly", tasks.named("functionalTest"))
}
tasks.jacocoTestReport {
    shouldRunAfter(tasks.withType<Test>())
    executionData(
        fileTree("$buildDir/jacoco").apply {
            include("*.exec")
        }
    )
    reports {
        // xml report is used by codecov
        xml.required.set(true)
    }
}
