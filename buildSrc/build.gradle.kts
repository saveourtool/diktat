plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
    mavenLocal()
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
