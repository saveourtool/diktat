import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

repositories {
    file("$rootDir/../../build/diktat-snapshot")
        .takeIf { it.exists() }
        ?.run {
            maven {
                url = this@run.toURI()
            }
        }
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // workaround https://github.com/gradle/gradle/issues/15383
    implementation(files(project.libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.reckon.gradle.plugin)
    implementation(libs.detekt.gradle.plugin) {
        exclude("io.github.detekt.sarif4k", "sarif4k")
    }
    implementation(libs.diktat.gradle.plugin) {
        exclude("io.github.detekt.sarif4k", "sarif4k")
    }
    implementation(libs.sarif4k)
    implementation(libs.gradle.plugin.spotless)
    implementation(libs.publish.gradle.plugin)
    // extra dependencies
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.stdlib.common)
    implementation(libs.kotlin.stdlib.jdk7)
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.jetbrains.annotations)
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
    }
}
