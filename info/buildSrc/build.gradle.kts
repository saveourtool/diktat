plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    mavenLocal {
        content {
            includeGroup("org.cqfn.diktat")
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
    implementation("org.cqfn.diktat:diktat-rules:$version")
}
