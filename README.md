<img src="/logo.svg" width="64px"/>

![Build and test](https://github.com/cqfn/diKTat/workflows/Build%20and%20test/badge.svg)
![deteKT static analysis](https://github.com/cqfn/diKTat/workflows/Run%20deteKT/badge.svg)
![diKTat code style](https://github.com/cqfn/diKTat/workflows/Run%20diKTat/badge.svg)
[![License](https://img.shields.io/github/license/cqfn/diKtat)](https://github.com/cqfn/diKTat/blob/master/LICENSE)
[![codecov](https://codecov.io/gh/cqfn/diKTat/branch/master/graph/badge.svg)](https://codecov.io/gh/cqfn/diKTat)

[![Releases](https://img.shields.io/github/v/release/cqfn/diKTat)](https://github.com/cqfn/diKTat/releases)
![Maven Central](https://img.shields.io/maven-central/v/org.cqfn.diktat/diktat-rules)
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fcqfn%2FdiKTat.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2Fcqfn%2FdiKTat?ref=badge_shield)
[![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/)
[![Chat on Telegram](https://img.shields.io/badge/Chat%20on-Telegram-brightgreen.svg)](https://t.me/joinchat/AAAAAFDg-ipuZFGyBGPPeg)

[![Hits-of-Code](https://hitsofcode.com/github/cqfn/diktat)](https://hitsofcode.com/view/github/cqfn/diktat)
![Lines of code](https://img.shields.io/tokei/lines/github/cqfn/diktat)
![GitHub repo size](https://img.shields.io/github/repo-size/cqfn/diktat)
[![Awesome Kotlin Badge](https://kotlin.link/awesome-kotlin.svg)](https://github.com/KotlinBy/awesome-kotlin)


DiKTat is a strict [coding standard ](info/guide/diktat-coding-convention.md) for Kotlin and a collection of [Kotlin](https://kotlinlang.org/) code style rules implemented
as AST visitors on the top of [KTlint](https://ktlint.github.io/). It can be used for detecting and autofixing code smells in CI/CD process. 
The full list of available supported rules and inspections can be found [here](info/available-rules.md).

Now diKTat was already added to the lists of [static analysis tools](https://github.com/analysis-tools-dev/static-analysis) and to [kotlin-awesome](https://github.com/KotlinBy/awesome-kotlin). Thanks to the community for this support! 

## See first

|  |  |  |  |
| --- | --- | --- | --- |
|[DiKTat codestyle](info/guide/diktat-coding-convention.md)|[Supported Rules](info/available-rules.md) | [Examples of Usage](https://github.com/akuleshov7/diktat-examples) | [Online Demo](https://ktlint-demo.herokuapp.com) |

## Why should I use diktat in my CI/CD?

There are several tools like `detekt` and `ktlint` that are doing static analysis. Why do I need diktat?

First of all - actually you can combine diktat with any other static analyzers. And diKTat is even using ktlint framework for parsing the code into the AST.
And we are trying to contribute to those projects. 
Main features of diktat are the following:

1) **More inspections.** It has 100+ inspections that are tightly coupled with it's codestyle.

2) **Unique inspections** that are missing in other linters.

3) **Highly configurable**. Each and every inspection can be configured and suppressed both from the code or from the configuration file.

4) **Strict detailed coding convention** that you can use in your project.

## Run as CLI-application
1. Install KTlint manually: [here](https://github.com/pinterest/ktlint/releases)

   **OR** use curl:
    ```bash
    curl -sSLO https://github.com/pinterest/ktlint/releases/download/0.39.0/ktlint && chmod a+x ktlint
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
                  <version>0.39.0</version>
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
This plugin should be available since version 0.1.3. It requires less configuration but may contain bugs.
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

## Run with Gradle 

You can see how it is configured in our project for self-checks: [build.gradle.kts](build.gradle.kts).
Add the code below to your `build.gradle.kts`:
<details>
  <summary><b>Gradle snippet</b></summary><br>
  
```kotlin
object Versions {
    const val ktlint = "0.39.0"
    const val diktat = "0.1.3"
}

tasks {
    val ktlint: Configuration by configurations.creating
    val diktatConfig: JavaExec.() -> Unit = {
        group = "diktat"
        classpath = ktlint
        main = "com.pinterest.ktlint.Main"

        inputs.files(project.fileTree(mapOf("dir" to "src", "include" to "**/*.kt")))
        outputs.dir("${project.buildDir}/reports/diktat/")

        outputs.upToDateWhen { false }  // Allows to run the task again (otherwise skipped till sources are changed).
        isIgnoreExitValue = true  // Allows to skip the non-zero exit code, can be useful when other tasks depends on linter

        dependencies {
            ktlint("com.pinterest:ktlint:${Versions.ktlint}") {
                exclude("com.pinterest.ktlint", "ktlint-ruleset-standard")
            }

            ktlint("org.cqfn.diktat:diktat-rules:${Versions.diktat}")
        }
    }

    create<JavaExec>("diktatCheck") {
        diktatConfig()
        description = "Check Kotlin code style."

        args = listOf("src/*/kotlin/**/*.kt")    // specify proper path to sources that should be checked here
    }

    create<JavaExec>("diktatFormat") {
        diktatConfig()
        description = "Fix Kotlin code style deviations."

        args = listOf("-F", "src/main/kotlin/**/*.kt")  // specify proper path to sources that should be checked here
    }
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
