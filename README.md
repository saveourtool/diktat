<img src="/logo.svg" width="64px"/>

![Build and test](https://github.com/cqfn/diKTat/workflows/Build%20and%20test/badge.svg)
![deteKT static analysis](https://github.com/cqfn/diKTat/workflows/Run%20deteKT/badge.svg)
![diKTat code style](https://github.com/cqfn/diKTat/workflows/Run%20diKTat/badge.svg)

[![Releases](https://img.shields.io/github/v/release/cqfn/diKTat)](https://github.com/cqfn/diKTat/releases)
![Maven Central](https://img.shields.io/maven-central/v/org.cqfn.diktat/diktat-rules)
[![License](https://img.shields.io/github/license/cqfn/diKtat)](https://github.com/cqfn/diKTat/blob/master/LICENSE)
[![codecov](https://codecov.io/gh/cqfn/diKTat/branch/master/graph/badge.svg)](https://codecov.io/gh/cqfn/diKTat)

[![Hits-of-Code](https://hitsofcode.com/github/cqfn/diktat)](https://hitsofcode.com/view/github/cqfn/diktat)
![Lines of code](https://img.shields.io/tokei/lines/github/cqfn/diktat)
![GitHub repo size](https://img.shields.io/github/repo-size/cqfn/diktat)

[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fcqfn%2FdiKTat.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2Fcqfn%2FdiKTat?ref=badge_shield)
[![Awesome Kotlin Badge](https://kotlin.link/awesome-kotlin.svg)](https://github.com/KotlinBy/awesome-kotlin)
[![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/)
[![Chat on Telegram](https://img.shields.io/badge/Chat%20on-Telegram-brightgreen.svg)](https://t.me/joinchat/AAAAAFDg-ipuZFGyBGPPeg)

### (!) See [diKTat codestyle](info/diktat-kotlin-coding-style-guide-en.md) first.
### (!) Also see [the list of all supported rules](info/available-rules.md).
### (!) Have a look at [maven and gradle examples](https://github.com/akuleshov7/diktat-examples).
### (!) Check and try diktat/ktlint [online demo](https://ktlint-demo.herokuapp.com)

DiKTat is a collection of [Kotlin](https://kotlinlang.org/) code style rules implemented
as AST visitors on top of [KTlint](https://ktlint.github.io/).
The full list of available supported rules and inspections is [here](info/available-rules.md).

## Run as CLI-application
1. Install KTlint manually: [here](https://github.com/pinterest/ktlint/releases)

   **OR** use curl:
    ```bash
    curl -sSLO https://github.com/pinterest/ktlint/releases/download/0.37.1/ktlint && chmod a+x ktlint
    # another option is "brew install ktlint"
    ```
   
2. Load diKTat manually: [here](https://github.com/cqfn/diKTat/releases/download/v0.1.1/diktat.jar)

   **OR** use curl:
   ```bash
   $ curl -sSLO https://github.com/cqfn/diKTat/releases/download/v0.1.1/diktat-0.1.1.jar
   ```
   
3. Finally, run KTlint (with diKTat injected) to check your `*.kt` files in `dir/your/dir`:
   ```bash
   $ ./ktlint -R diktat.jar "dir/your/dir/**/*.kt"
   ```

To **autofix** all code style violations use `-F` option.

## Run with Maven

### Use maven-antrun-plugin

Add this plugin to your pom.xml:
<details>
  <summary><b>Maven plugin snippet</b></summary><br>
  
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
                  <version>0.37.1</version>
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
                  <version>0.1.1</version> <!-- replace it with diktat latest version -->
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

</details>

In case you want to add autofixer with diKTat ruleset just extend
the snippet above with `<arg value="-F"/>`.

To run diktat to check/fix code style - run `$ mvn antrun:run@diktat`.

### Use the new diktat-maven-plugin

You can see how it is configured in our project for self-checks: [pom.xml](pom.xml).
This plugin should be available since version 0.1.2. It requires less configuration but may contain bugs.
If you use it and encounter any problems, feel free to open issues on [github](https://github.com/cqfn/diktat/issues).

Add this plugin to your pom.xml:
<details>
  <summary><b>Maven plugin snippet</b></summary><br>
  
```xml
            <plugin>
                <groupId>org.cqfn.diktat</groupId>
                <artifactId>diktat-maven-plugin</artifactId>
                <version>${diktat.version}</version>
                <executions>
                    <execution>
                        <id>diktat</id>
                        <phase>none</phase>
                        <goals>
                            <goal>check</goal>
                            <goal>fix</goal>
                        </goals>
                        <configuration>
                            <inputs>
                                <input>${project.basedir}/src/main/kotlin</input>
                                <input>${project.basedir}/src/test/kotlin</input>
                            </inputs>
                            <diktatConfigFile>diktat-analysis.yml</diktatConfigFile>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
```

</details>

To run diktat check use command `$ mvn diktat:check@diktat`.
To run diktat in autocorrect mode use command `$ mvn diktat:fix@diktat`.

## Run with Gradle Plugin 

You can see how it is configured in our project for self-checks: [build.gradle.kts](build.gradle.kts).
Add the code below to your `build.gradle.kts`:
<details>
  <summary><b>Gradle plugin snippet</b></summary><br>
  
```kotlin
val ktlint by configurations.creating

dependencies {
    ktlint("com.pinterest:ktlint:0.37.1") {
        // need to exclude standard ruleset to use only diktat rules
        exclude("com.pinterest.ktlint", "ktlint-ruleset-standard")
    }

    // diktat ruleset
    ktlint("org.cqfn.diktat:diktat-rules:0.1.1")
}

val outputDir = "${project.buildDir}/reports/diktat/"
val inputFiles = project.fileTree(mapOf("dir" to "src", "include" to "**/*.kt"))

val diktatCheck by tasks.creating(JavaExec::class) {
    inputs.files(inputFiles)
    outputs.dir(outputDir)

    description = "Check Kotlin code style."
    classpath = ktlint
    main = "com.pinterest.ktlint.Main"

    // specify proper path to sources that should be checked here
    args = listOf("src/main/kotlin/**/*.kt")
}

val diktatFormat by tasks.creating(JavaExec::class) {
    inputs.files(inputFiles)
    outputs.dir(outputDir)

    description = "Fix Kotlin code style deviations."
    classpath = ktlint
    main = "com.pinterest.ktlint.Main"

    // specify proper path to sources that should be checked here
    args = listOf("-F", "src/main/kotlin/**/*.kt")
}
```

</details>

To run diktat to check/fix code style - run `$ gradle diktatCheck`.

## Customizations via `diktat-analysis.yml`

In KTlint, rules can be configured via `.editorconfig`, but
this does not give a chance to customize or enable/disable
each and every rule independently.
That is why we have supported `diktat-analysis.yml` that can be easily
changed and help in customization of your own rule set.
It has simple fields:
`name` — name of the rule,
`enabled` (true/false) — to enable or disable that rule (all rules are enabled by the default),
`configuration` — a simple map of some extra unique configurations for this particular rule.
For example:

```yaml
- name: HEADER_MISSING_OR_WRONG_COPYRIGHT
  # all rules are enabled by the default. To disable add 'enabled: false' to the config.
  enabled: true 
  configuration:
    isCopyrightMandatory: true,
    copyrightText: Copyright (c) Jeff Lebowski, 2012-2020. All rights reserved.
```
Note, that you can specify and put `diktat-analysis.yml` that contains configuration of diktat in the parent directory of your project on the same level where `build.gradle/pom.xml` is stored. \
See default configuration in [diktat-analysis.yml](diktat-rules/src/main/resources/diktat-analysis.yml) \
Also see [the list of all rules supported by diKTat](info/available-rules.md).

## Suppress warnings on individual code blocks
In addition to enabling/disabling warning globally via config file (`enable = false`), you can suppress warnings by adding `@Suppress` annotation on individual code blocks

For example:

``` kotlin
@Suppress("FUNCTION_NAME_INCORRECT_CASE")
class SomeClass {
    fun methODTREE(): String {

    }
}
``` 
## How to contribute?

Main components are:
1) diktat-rules — number of rules that are supported by diKTat;
2) diktat-test-framework — functional/unit test framework that can be used for running your code fixer on the initial code and compare it with the expected result;
3) also see our demo: diktat-demo in a separate repository.

Mainly we wanted to create a common configurable mechanism that
will give us a chance to enable/disable and customize all rules.
That's why we added logic for:
1) Parsing `.yml` file with configurations of rules and passing it to visitors;
2) Passing information about properties to visitors.
This information is very useful, when you are trying to get,
for example, a filename of file where the code is stored;
3) We added a bunch of visitors, checkers and fixers that will extended KTlint functionaliity with code style rules;
4) We have proposed a code style for Kotlin language. 

Before you make a pull request, make sure the build is clean as we have lot of tests and other prechecks:

```bash
$ mvn clean install
```

Also see our [Contributing Policy](CONTRIBUTING.md) and [Code of Conduct](CODE_OF_CONDUCT.md)
