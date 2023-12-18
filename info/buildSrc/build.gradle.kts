plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    mavenLocal {
        content {
            includeGroup("com.saveourtool.diktat")
        }
    }
    flatDir {
        dirs(
            "$rootDir/../../diktat-rules/target",
            "$rootDir/../../diktat-common/target"
        )
    }
}

dependencies {
    implementation("com.saveourtool.diktat:diktat-rules:$version")
}
