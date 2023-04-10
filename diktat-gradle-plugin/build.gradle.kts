import org.cqfn.diktat.buildutils.configureSigning
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentOperatingSystem
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.cqfn.diktat.buildutils.kotlin-jvm-configuration")
    id("org.cqfn.diktat.buildutils.code-quality-convention")
    id("pl.droidsonroids.jacoco.testkit") version "1.0.9"
    id("org.gradle.test-retry") version "1.5.2"
    id("com.gradle.plugin-publish") version "1.2.0"
}

dependencies {
    implementation(kotlin("gradle-plugin-api"))

    implementation(projects.diktatRules)
    implementation(projects.diktatKtlintEngine)
    // merge sarif reports
    implementation(libs.sarif4k.jvm)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.ktlint.reporter.json)
    testImplementation(libs.ktlint.reporter.plain)
    testImplementation(libs.ktlint.reporter.sarif)
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        // fixme: kotlin 1.3 is required for gradle <6.8
        languageVersion = "1.3"
        apiVersion = "1.3"
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs - "-Werror"
    }
}

gradlePlugin {
    plugins {
        create("diktatPlugin") {
            id = "org.cqfn.diktat.diktat-gradle-plugin"
            implementationClass = "org.cqfn.diktat.plugin.gradle.DiktatGradlePlugin"
        }
    }
}

// === testing & code coverage, jacoco is run independent from maven
val functionalTestTask by tasks.register<Test>("functionalTest")
tasks.withType<Test> {
    useJUnitPlatform()
}

// === integration testing
// fixme: should probably use KotlinSourceSet instead
val functionalTest: SourceSet = sourceSets.create("functionalTest") {
    compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath.get()
    runtimeClasspath += output + compileClasspath
}

@Suppress("GENERIC_VARIABLE_WRONG_DECLARATION", "MAGIC_NUMBER")
val functionalTestProvider: TaskProvider<Test> = tasks.named<Test>("functionalTest") {
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
    @Suppress("UNCHECKED_CAST")
    applyTo("functionalTestRuntimeOnly", functionalTestProvider as TaskProvider<Task>)
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

configureSigning()
