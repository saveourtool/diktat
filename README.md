<img src="/logo.svg" width="64px"/>

![Build and test](https://github.com/cqfn/diKTat/workflows/Build%20and%20test/badge.svg)
![deteKT static analysis](https://github.com/cqfn/diKTat/workflows/Run%20deteKT/badge.svg)
[![Releases](https://img.shields.io/github/v/release/cqfn/diKTat)](https://github.com/cqfn/diKTat/releases)
[![License](https://img.shields.io/github/license/cqfn/diKtat)](https://github.com/cqfn/diKTat/blob/master/LICENSE)
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fcqfn%2FdiKTat.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2Fcqfn%2FdiKTat?ref=badge_shield)
[![Awesome Kotlin Badge](https://kotlin.link/awesome-kotlin.svg)](https://github.com/KotlinBy/awesome-kotlin)

[![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/)
[![Chat on Telegram](https://img.shields.io/badge/Chat%20on-Telegram-brightgreen.svg)](https://t.me/joinchat/AAAAAFDg-ipuZFGyBGPPeg)
[![Hits-of-Code](https://hitsofcode.com/github/cqfn/diktat)](https://hitsofcode.com/view/github/cqfn/diktat)
[![codecov](https://codecov.io/gh/cqfn/diKTat/branch/master/graph/badge.svg)](https://codecov.io/gh/cqfn/diKTat)

It is a rule set of Kotlin code style rules that are using
[KTlint](https://ktlint.github.io/) framework under the hood.
In this project we are trying to define Kotlin code style
rules and implement them as visitors for AST tree provided by KTlint.
It will detect (check) and fix code style issues based on
[our custom codestyle](https://github.com/cqfn/diKTat/wiki/diKTat-codestyle-guide).

Read how [KTlint](https://ktlint.github.io/) works first.

First, load KTlint (you can also download it manually
from [ktlint project repo](https://github.com/pinterest/ktlint/releases)
or use `brew install ktlint`):

```bash
$ curl -sSLO https://github.com/pinterest/ktlint/releases/download/0.37.2/ktlint && chmod a+x ktlint`
```

Then, load diKTat:

```bash
$ curl -sSLO https://central.artipie.com/akuleshov7/diktat/org/cqfn/diktat/diktat-rules/0.0.1/diktat-rules-0.0.1-jar-with-dependencies.jar
```

Finally, run KTlint with diKTat injected:

```bash
$ ./ktlint -R diktat-rules-0.0.1-jar-with-dependencies.jar "src/test/**/*.kt"
```

This will run the default configuration of diKTat in check mode.

To start autofixing use `-F` option.

If in trouble, try this:

`./ktlint -help`

## Maven Plugin

Currently diKTat releases are hosted on the
[Artipie](https://www.artipie.com/), so you need to add lines below to your pom.xml file:

```xml
<project>
  [...]
  <distributionManagement>
    <repository>
      <id>artipie</id>
      <url>https://central.artipie.com/akuleshov7/diktat</url>
    </repository>
    <snapshotRepository>
      <id>artipie</id>
      <url>https://central.artipie.com/akuleshov7/diktat</url>
    </snapshotRepository>
  </distributionManagement>
  <repositories>
    <repository>
      <id>artipie</id>
      <url>https://central.artipie.com/akuleshov7/diktat</url>
    </repository>
  </repositories>
</project>
```

Add this snippet to your pom.xml:

```xml
<project>
  [...]
  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-antrun-plugin</artifactId>
        <version>1.8</version>
        <executions>
          <execution>
            <id>ktlint</id>
            <phase>verify</phase>
            <configuration>
              <target name="ktlint">
                <java taskname="ktlint" dir="${basedir}" fork="true" failonerror="true"
                  classpathref="maven.plugin.classpath" classname="com.pinterest.ktlint.Main">
                  <arg value="src/**/*.kt"/>
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
            <version>0.37.2</version>
          </dependency>
          <dependency>
            <groupId>org.cqfn.diktat</groupId>
            <artifactId>diktat-rules</artifactId>
            <version>0.0.1-SNAPSHOT</version>
          </dependency>
        </dependencies>
      </plugin>
    </plugins>
  </build>
</project>
```

In case you want to add autofixer with diktat ruleset just extend
the snippet above with `<arg value="-F"/>`.

## Customizations via `rules-config.json`

In ktlint rules can be configured via `.editorconfig`, but
this does not give a chance to customize or enable/disable
each and every rule independently.
That is why we have supported `rules-config.json` that can be easily
changed and help in customization of your own rule set.
It has simple fields: `name` - name of the rule, `enabled` (true/false)
to enable or disable that rule, and `configuration` - a simple map
of some extra unique configurations for the rule, for example:

```json
"configuration": {
  "isCopyrightMandatory": true,
  "copyrightText": "Copyright (c) Jeff Lebowski, 2012-2020. All rights reserved."
}
```

## How to contribute?

Main components are:

1) diktat-ruleset - number of rules that are supported by diKTat
2) diktat-test-framework - functional/unit test framework that can be used for running your code fixer on the initial code and compare it with the expected result
3) also see our demo: diktat-demo in a separate repository

Mainly we wanted to create a common configurable mechanism that will give us a chance to enable/disable and customize all rules.
That's why we added logic for:
1) Parsing .json file with configurations of rules and passing it to visitors
2) Passing information about properties to visitors. This information is very useful, when you are trying to get, for example, a filename of file where the code is stored.
3) We added a bunch of visitors that will extended KTlint functionaliity

Download:

```bash
$ git clone https://github.com/akuleshov7/diKTat.git
```

We are using maven as we tired of Gradle:
```bash
$ mvn clean install
```

This will also install git hooks into your local .git directory. The hooks will restrict commit messages and branch naming.

Please follow our [contributing policy](info/CONTRIBUTING.md)
