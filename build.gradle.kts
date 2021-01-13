import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version Versions.Kotlin apply false
    kotlin("plugin.serialization") version Versions.Kotlin apply false
}

allprojects {
    repositories {
        jcenter()
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
    configureDiktat()
}

tasks.register("diktatCheckAll") {
    group = "verification"
    allprojects {
        this@register.dependsOn(tasks.getByName("diktatCheck"))
    }
}
