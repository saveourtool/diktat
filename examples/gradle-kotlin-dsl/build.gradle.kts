plugins {
    id("org.cqfn.diktat.diktat-gradle-plugin") version "0.1.5"
}

diktat {
    inputs = files("src/**/*.kt")
}
