plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
}

dependencies {
    implementation("org.cqfn.diktat:diktat-rules:0.3.0")  // todo correct version of current build?
    implementation("org.cqfn.diktat:diktat-gradle-plugin:0.3.0")
}
