<img src="/logo.svg" width="64px"/>

![Build and test](https://github.com/cqfn/diKTat/workflows/Build%20and%20test/badge.svg)
![deteKT static analysis](https://github.com/cqfn/diKTat/workflows/Run%20deteKT/badge.svg)
![diKTat code style](https://github.com/cqfn/diKTat/workflows/Run%20diKTat/badge.svg)

[![Releases](https://img.shields.io/github/v/release/cqfn/diKTat)](https://github.com/cqfn/diKTat/releases)
[![License](https://img.shields.io/github/license/cqfn/diKtat)](https://github.com/cqfn/diKTat/blob/master/LICENSE)
[![Hits-of-Code](https://hitsofcode.com/github/cqfn/diktat)](https://hitsofcode.com/view/github/cqfn/diktat)
[![codecov](https://codecov.io/gh/cqfn/diKTat/branch/master/graph/badge.svg)](https://codecov.io/gh/cqfn/diKTat)

[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fcqfn%2FdiKTat.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2Fcqfn%2FdiKTat?ref=badge_shield)
[![Awesome Kotlin Badge](https://kotlin.link/awesome-kotlin.svg)](https://github.com/KotlinBy/awesome-kotlin)
[![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/)
[![Chat on Telegram](https://img.shields.io/badge/Chat%20on-Telegram-brightgreen.svg)](https://t.me/joinchat/AAAAAFDg-ipuZFGyBGPPeg)

## (!) See [diKTat codestyle](info/diktat-kotlin-coding-style-guide-en.md) first.

DiKTat is a collection of [Kotlin](https://kotlinlang.org/) code style rules implemented
as AST visitors on top of [KTlint](https://ktlint.github.io/).
The full list of available supported rules and inspections is [here](info/available-rules.md).

## Run as CLI-application
1. Install KTlint (until this [PR](https://github.com/pinterest/ktlint/pull/806) is merged you will need to use
 [KTlint fork](https://central.artipie.com/akuleshov7/files/ktlint)):
   ```bash
   $ curl -sSLO https://central.artipie.com/akuleshov7/files/ktlint && chmod a+x ktlint
   ```
   
2. Load diKTat manually: [here](https://github.com/cqfn/diKTat/releases/download/v1.0.1/diktat.jar)

   **OR** use curl:
   ```bash
   $ curl -sSLO https://github.com/cqfn/diKTat/releases/download/v1.0.1/diktat.jar
   ```
   
3. Finally, run KTlint (with diKTat injected) to check your `*.kt` files in `dir/your/dir`:
   ```bash
   $ ./ktlint -R diktat.jar "dir/your/dir/**/*.kt"
   ```

To autofix all violations use `-F` option.

## Run with Maven Plugin

First, add this to your `pom.xml` file:

```xml
<project>
  [...]
  <repositories>
    <repository>
      <id>artipie</id>
      <url>https://central.artipie.com/akuleshov7/diktat</url>
    </repository>
  </repositories>
    <pluginRepositories>
      <pluginRepository>
        <id>artipie</id>
        <url>https://central.artipie.com/akuleshov7/diktat</url>
      </pluginRepository>
    </pluginRepositories>
</project>
```

Then, add this plugin:

```xml
<project>
  [...]
  <build>
    <plugins>
      <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-antrun-plugin</artifactId>
          <version>3.0.0</version>
          <executions>
              <execution>
                  <id>diktat</id>
                  <phase>none</phase>
                  <configuration>
                      <target name="ktlint">
                          <java taskname="ktlint" dir="${basedir}" fork="true" failonerror="true"
                                classpathref="maven.plugin.classpath" classname="com.pinterest.ktlint.Main">
                              <arg value="src/main/**/*.kt"/>
                              <arg value="src/test/kotlin/**/*.kt"/>
                          </java>
                      </target>
                  </configuration>
                  <goals>
                      <goal>run</goal>
                  </goals>
              </execution>
          </executions>
          <dependencies>
              <dependency>
                  <groupId>com.pinterest</groupId>
                  <artifactId>ktlint</artifactId>
                  <version>0.37.1-fork</version> <!-- use this fork to be compatible with diktat -->
                  <exclusions>
                      <exclusion>  <!-- without this exclusion both rulesets are enabled which we discourage -->
                          <groupId>com.pinterest.ktlint</groupId>
                          <artifactId>ktlint-ruleset-standard</artifactId>
                      </exclusion>
                  </exclusions>
              </dependency>
              <dependency>
                  <groupId>org.cqfn.diktat</groupId>
                  <artifactId>diktat-rules</artifactId>
                  <version>1.0.1</version> <!-- replace it with diktat latest version -->
                  <exclusions>
                      <exclusion>
                          <groupId>org.slf4j</groupId>
                          <artifactId>slf4j-log4j12</artifactId>
                      </exclusion>
                  </exclusions>
              </dependency>
          </dependencies>
      </plugin>
    </plugins>
  </build>
</project>
```

In case you want to add autofixer with diKTat ruleset just extend
the snippet above with `<arg value="-F"/>`.

To run diktat to check/fix code style - run `mvn antrun:run@diktat`.

## Gradle Kotlin
`build.gradle.kts`
```kotlin
val ktlint by configurations.creating

dependencies {
    ktlint("com.pinterest:ktlint:0.37.2")
    // ktlint(project(":custom-ktlint-ruleset")) // in case of custom ruleset
}

val outputDir = "${project.buildDir}/reports/ktlint/"
val inputFiles = project.fileTree(mapOf("dir" to "src", "include" to "**/*.kt"))

val ktlintCheck by tasks.creating(JavaExec::class) {
    inputs.files(inputFiles)
    outputs.dir(outputDir)

    description = "Check Kotlin code style."
    classpath = ktlint
    main = "com.pinterest.ktlint.Main"
    args = listOf("src/**/*.kt")
}

val ktlintFormat by tasks.creating(JavaExec::class) {
    inputs.files(inputFiles)
    outputs.dir(outputDir)

    description = "Fix Kotlin code style deviations."
    classpath = ktlint
    main = "com.pinterest.ktlint.Main"
    args = listOf("-F", "src/**/*.kt")
}
```

## Gradle Groovy
`build.gradle`
```groovy
// kotlin-gradle-plugin must be applied for configuration below to work
// (see https://kotlinlang.org/docs/reference/using-gradle.html)

apply plugin: 'java'

repositories {
    jcenter()
}

configurations {
    ktlint
}

dependencies {
    ktlint "com.pinterest:ktlint:0.37.2"
    // additional 3rd party ruleset(s) can be specified here
    // just add them to the classpath (e.g. ktlint 'groupId:artifactId:version') and 
    // ktlint will pick them up
}

task ktlint(type: JavaExec, group: "verification") {
    description = "Check Kotlin code style."
    classpath = configurations.ktlint
    main = "com.pinterest.ktlint.Main"
    args "src/**/*.kt"
    // to generate report in checkstyle format prepend following args:
    // "--reporter=plain", "--reporter=checkstyle,output=${buildDir}/ktlint.xml"
    // see https://github.com/pinterest/ktlint#usage for more
}
check.dependsOn ktlint

task ktlintFormat(type: JavaExec, group: "formatting") {
    description = "Fix Kotlin code style deviations."
    classpath = configurations.ktlint
    main = "com.pinterest.ktlint.Main"
    args "-F", "src/**/*.kt"
}
```
## Customizations via `rules-config.json`

In KTlint, rules can be configured via `.editorconfig`, but
this does not give a chance to customize or enable/disable
each and every rule independently.
That is why we have supported `rules-config.json` that can be easily
changed and help in customization of your own rule set.
It has simple fields:
`name` — name of the rule,
`enabled` (true/false) — to enable or disable that rule, and
`configuration` — a simple map of some extra unique configurations for the rule.
For example:

```json
"configuration": {
  "isCopyrightMandatory": true,
  "copyrightText": "Copyright (c) Jeff Lebowski, 2012-2020. All rights reserved."
}
```

See default configuration in [rules-config.json](diktat-rules/src/main/resources/rules-config.json)

## How to contribute?

Main components are:
1) diktat-ruleset — number of rules that are supported by diKTat;
2) diktat-test-framework — functional/unit test framework that can be used for running your code fixer on the initial code and compare it with the expected result;
3) also see our demo: diktat-demo in a separate repository.

Mainly we wanted to create a common configurable mechanism that
will give us a chance to enable/disable and customize all rules.
That's why we added logic for:
1) Parsing `.json` file with configurations of rules and passing it to visitors;
2) Passing information about properties to visitors.
This information is very useful, when you are trying to get,
for example, a filename of file where the code is stored;
3) We added a bunch of visitors that will extended KTlint functionaliity.

Before you make a pull request, make sure the build is clean:

```bash
$ mvn clean install
```

Also see our [Contributing Policy](CONTRIBUTING.md) and [Code of Conduct](CODE_OF_CONDUCT.md)