## Building
Build of gradle plugin is performed by gradle, but is wrapped in maven build. The module's `pom.xml` isn't exactly accurate
and doesn't include gradle-specific dependencies, that are automatically provided by gradle when applying the plugin.

To avoid versions duplication, diktat and ktlint versions are passed to gradle via properties when running gradle task from maven.
These versions are then written in a file and then included in the plugin jar to determine dependencies for JavaExec.

Gradle plugin marker pom, which is normally produced by `java-gradle-plugin` plugin during gradle build,
is added manually as a maven module.

Please be advised that to run functional tests of Gradle plugin you will need to have Java 11 or **older**.
This does not affect the plugin itself and only affect functional tests :
```
Starting Build
java.lang.NoClassDefFoundError: Could not initialize class org.codehaus.groovy.vmplugin.v7.Java7
```
