plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":diktat-common"))
    implementation("com.pinterest.ktlint:ktlint-core:${Versions.KtLint}")
    implementation("com.google.guava:guava:${Versions.Guava}")
    implementation("org.slf4j:slf4j-api:${Versions.Slf4j}")
    implementation("org.slf4j:slf4j-log4j12:${Versions.Slf4j}")

    testImplementation(project(":diktat-test-framework"))
    testImplementation("org.mockito:mockito-all:1.10.19")
}

configureJunit()

val generators: Configuration by configurations.creating {
    dependencies.add(
        project.dependencies.create("com.squareup:kotlinpoet:1.7.1")
    )
    dependencies.addAll(configurations.implementation.get().dependencies)
}
sourceSets.create("generators") {
    compileClasspath += sourceSets.main.get().output + generators
    runtimeClasspath += sourceSets.main.get().output + generators
}

tasks.register("generateWarningNames", JavaExec::class) {
    dependsOn("compileKotlin")
    classpath(sourceSets.getByName("generators").runtimeClasspath)
    mainClass.set("org.cqfn.diktat.ruleset.generation.GenerationKt")
    workingDir(project.projectDir)  // to ensure path to files can be set relative to diktat-rules
}

tasks.getByName("compileTestKotlin").dependsOn("generateWarningNames")
