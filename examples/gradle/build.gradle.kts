val ktlint by configurations.creating

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    ktlint("com.pinterest:ktlint:0.37.1") {
        // need to exclude standard ruleset to use only diktat rules
        exclude("com.pinterest.ktlint", "ktlint-ruleset-standard")
    }

    // diktat ruleset
    ktlint("org.cqfn.diktat:diktat-rules:0.1.0") {
    }
}

val outputDir = "${project.buildDir}/reports/diktat/"
val inputFiles = project.fileTree(mapOf("dir" to "src", "include" to "**/*.kt"))

val diktatCheck by tasks.creating(JavaExec::class) {
    inputs.files(inputFiles)
    outputs.dir(outputDir)

    description = "Check Kotlin code style."
    classpath = ktlint
    main = "com.pinterest.ktlint.Main"

    // specify proper path to sources that should be checked here
    args = listOf("src/main/kotlin/**/*.kt")
}

val diktatFormat by tasks.creating(JavaExec::class) {
    inputs.files(inputFiles)
    outputs.dir(outputDir)

    description = "Fix Kotlin code style deviations."
    classpath = ktlint
    main = "com.pinterest.ktlint.Main"

    // specify proper path here
    args = listOf("-F","src/main/kotlin/**/*.kt")
}