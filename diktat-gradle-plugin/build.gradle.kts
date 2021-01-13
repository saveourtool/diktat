import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentOperatingSystem

plugins {
    `java-gradle-plugin`
    kotlin("jvm")
    jacoco
    id("pl.droidsonroids.jacoco.testkit") version "1.0.7"
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("gradle-plugin-api"))

    implementation("com.pinterest.ktlint:ktlint-core:${Versions.KtLint}") {
        exclude("com.pinterest.ktlint", "ktlint-ruleset-standard")
    }
    implementation("com.pinterest.ktlint:ktlint-reporter-plain:${Versions.KtLint}")
    implementation(project(":diktat-rules"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.Junit}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.Junit}")
}

val generateVersionsFile by tasks.registering {
    val versionsFile = File("$buildDir/generated/src/generated/Versions.kt")

    outputs.file(versionsFile)

    doFirst {
        versionsFile.parentFile.mkdirs()
        versionsFile.writeText(
            """
            package generated

            internal const val DIKTAT_VERSION = "$version"
            internal const val KTLINT_VERSION = "${Versions.KtLint}"

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
val jacocoMergeTask by tasks.register<JacocoMerge>("jacocoMerge")
tasks.withType<Test> {
    useJUnitPlatform()
}
jacoco.toolVersion = Versions.Jacoco

// === integration testing
// fixme: should probably use KotlinSourceSet instead
val functionalTest = sourceSets.create("functionalTest") {
    compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath
    runtimeClasspath += output + compileClasspath
}
tasks.getByName<Test>("functionalTest") {
    dependsOn("test")
    testClassesDirs = functionalTest.output.classesDirs
    classpath = functionalTest.runtimeClasspath
    doLast {
        if (getCurrentOperatingSystem().isWindows) {
            // workaround for https://github.com/koral--/jacoco-gradle-testkit-plugin/issues/9
            logger.lifecycle("Sleeping for 5 sec after functionalTest to avoid error with file locking")
            Thread.sleep(5_000)
        }
    }
    finalizedBy(jacocoMergeTask)
}
tasks.check { dependsOn(tasks.jacocoTestReport) }
jacocoTestKit {
    applyTo("functionalTestRuntimeOnly", tasks.named("functionalTest"))
}
tasks.getByName("jacocoMerge", JacocoMerge::class) {
    dependsOn(functionalTestTask)
    executionData(
        fileTree("$buildDir/jacoco").apply {
            include("*.exec")
        }
    )
}
tasks.jacocoTestReport {
    dependsOn(jacocoMergeTask)
    executionData("$buildDir/jacoco/jacocoMerge.exec")
    reports {
        // xml report is used by codecov
        xml.isEnabled = true
    }
}
