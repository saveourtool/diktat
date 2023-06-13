import com.saveourtool.diktat.generation.docs.generateAvailableRules
import com.saveourtool.diktat.generation.docs.generateCodeStyle
import com.saveourtool.diktat.generation.docs.generateFullDoc
import com.saveourtool.diktat.generation.docs.generateRulesMapping

tasks.register("generateRulesMapping") {
    group = "documentation"
    description = "Generates a table (rules-mapping.md), which maps warning names to chapters in code style"
    doFirst {
        generateRulesMapping()
    }
}

tasks.register("generateFullDoc") {
    group = "documentation"
    description = "Compile individual chapters into a single markdown document"
    doFirst {
        generateFullDoc(file("$rootDir/guide"), "diktat-coding-convention.md")
    }
}

tasks.register("generateAvailableRules") {
    group = "documentation"
    description = "Generate table for White paper based on available-rules.md and rules-mapping.md"
    dependsOn("generateRulesMapping")
    doFirst {
        generateAvailableRules(rootDir, file("$rootDir/../wp"))
    }
}

tasks.register("generateCodeStyle") {
    group = "documentation"
    description = "Adds/updates diktat code style in white paper document"
    dependsOn("generateFullDoc")
    doFirst {
        generateCodeStyle(file("$rootDir/guide"), file("$rootDir/../wp"))
    }
}

tasks.register("updateMarkdownDocumentation") {
    group = "documentation"
    description = "Task that aggregates all documentation updates without white paper updates"
    dependsOn(
        "generateRulesMapping",
        "generateFullDoc"
    )
}

tasks.register("updateDocumentation") {
    group = "documentation"
    description = "Task that aggregates all documentation updates"
    dependsOn(
        "generateRulesMapping",
        "generateAvailableRules",
        "generateFullDoc",
        "generateCodeStyle"
    )
}
