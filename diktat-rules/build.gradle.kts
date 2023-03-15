import java.time.LocalDate
import java.nio.file.Files

plugins {
    id("com.saveourtool.save.buildutils.kotlin-jvm-configuration")
//    id("com.saveourtool.save.buildutils.code-quality-convention")
    id("com.google.devtools.ksp") version "1.8.0-1.0.8"
}

dependencies {
    api(projects.diktatCommon)
    testImplementation(projects.diktatTestFramework)
    api(libs.ktlint.core)
    implementation(libs.kotlin.stdlib.jdk8)
    // guava is used for string case utils
    implementation(libs.guava)
    implementation(libs.kotlin.logging)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.platform.suite)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito)
    // is used for simplifying boolean expressions
    implementation(libs.jbool.expressions)

    // generating
    implementation(projects.diktatDevKsp)
    ksp(projects.diktatDevKsp)
}

ksp {
    arg("sourceEnumName", "org.cqfn.diktat.ruleset.constants.Warnings")
    arg("targetPackageName", "generated")
    arg("targetClassName", "WarningNames")
}

val updateCopyrightYearInTestTaskProvider = tasks.register("updateCopyrightYearInTest") {
    val headerDir = "$projectDir/src/test/resources/test/paragraph2/header"
    inputs.dir(headerDir)
    outputs.dir(headerDir)

    val hyphenRegex = Regex("""\d+-\d+""")
    val afterCopyrightRegex = Regex("""((©|\([cC]\))+ *\d+)""")
    val curYear = LocalDate.now().year
    fileTree(headerDir)
        .filter { !it.name.contains("CopyrightDifferentYearTest.kt") }
        .map { it.toPath() }
        .forEach { file ->
            val updatedLines = Files.readAllLines(file).map { line ->
                when {
                    line.contains(hyphenRegex) -> line.replace(hyphenRegex) {
                        val years = it.value.split("-")
                        "${years[0]}-$curYear"
                    }
                    line.contains(afterCopyrightRegex) -> line.replace(afterCopyrightRegex) {
                        val copyrightYears = it.value.split("(c)", "(C)", "©")
                        "${copyrightYears[0]}-$curYear"
                    }
                    else -> line
                }
            }
            println("here")
            val tempFile = temporaryDir.toPath().resolve(file.fileName)
            Files.write(tempFile, updatedLines)
            Files.delete(file)
            Files.move(tempFile, file)
        }
}

tasks.named("compileTestKotlin") {
    dependsOn(updateCopyrightYearInTestTaskProvider)
}
