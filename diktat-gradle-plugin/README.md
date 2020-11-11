## Building
Build of gradle plugin is performed by gradle, but is wrapped in maven build.

Gradle plugin marker pom, which is normally produced by `java-gradle-plugin` plugin during gradle build,
is added manually as a maven module.

Build is skipped by default on windows to make things easier; if you want to build it, you should change
the property `gradle.executable` to `gradlew.bat` and the property `skip.gradle.build` to `true`.