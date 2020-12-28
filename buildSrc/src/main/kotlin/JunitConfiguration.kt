import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

fun Project.configureJunit() {
    this.dependencies {
        add("testImplementation", "org.junit.jupiter:junit-jupiter:${Versions.Junit}")
        add("testImplementation", "org.assertj:assertj-core:${Versions.Assert4J}")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}