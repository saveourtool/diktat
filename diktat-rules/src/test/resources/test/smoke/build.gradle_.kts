import org.cqfn.diktat.generation.docs.generateAvailableRules

plugins {
    kotlin("jvm") version "1.4.21"
    id("org.cqfn.diktat.diktat-gradle-plugin") version "0.1.7"
}

repositories {
    jcenter()
    mavenCentral()
    maven { url = uri("https://example.com") }
}

tasks.register("generateAvailableRules") {
    dependsOn("generateRulesMapping")
    doFirst {
        generateAvailableRules(rootDir, file("$rootDir/../wp"))
    }
}

tasks.register("updateDocumentation") {
    dependsOn(
        "generateRulesMapping",
        "generateAvailableRules",
        "generateFullDoc",
        "generateCodeStyle"
    )
}

diktat {
    debug = true
    inputs = files("buildSrc/**/*.kt", "*.kts")
}
