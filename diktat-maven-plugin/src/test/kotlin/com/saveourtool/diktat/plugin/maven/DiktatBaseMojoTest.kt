package com.saveourtool.diktat.plugin.maven

import org.apache.maven.execution.DefaultMavenExecutionRequest
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.testing.MojoRule
import org.apache.maven.project.ProjectBuilder
import org.apache.maven.project.ProjectBuildingRequest
import org.eclipse.aether.DefaultRepositorySystemSession
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions
import kotlin.io.path.Path
import kotlin.io.path.createTempFile
import kotlin.io.path.div

/**
 * Tests for mojo configuration. NB: this tests are using Junit4, because maven-plugin-testing-harness doesn't support 5.
 */
@Suppress("LongMethod", "TOO_LONG_FUNCTION")
@Ignore
class DiktatBaseMojoTest {
    @get:Rule val mojoRule = MojoRule()
    private lateinit var buildingRequest: ProjectBuildingRequest
    private lateinit var projectBuilder: ProjectBuilder

    /**
     * Initialize properties needed to create mavenProject stub able to resolve maven parameters.
     */
    @Before
    fun setUp() {
        val executionRequest = DefaultMavenExecutionRequest()
        buildingRequest = executionRequest.projectBuildingRequest.apply {
            repositorySession = DefaultRepositorySystemSession()
        }
        projectBuilder = mojoRule.lookup(ProjectBuilder::class.java)
    }

    @Test
    fun `test default plugin configuration`() {
        val pom = createTempFile().toFile()
        pom.writeText(
            """
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                        xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>

                    <groupId>com.saveourtool.diktat</groupId>
                    <artifactId>diktat-test</artifactId>
                    <version>1.0.0-SNAPSHOT</version>

                    <build>
                        <plugins>
                            <plugin>
                                <groupId>com.saveourtool.diktat</groupId>
                                <artifactId>diktat-maven-plugin</artifactId>
                                <executions>
                                    <execution>
                                        <goals>
                                            <goal>check</goal>
                                        </goals>
                                    </execution>
                                </executions>
                            </plugin>
                        </plugins>
                    </build>
                </project>
            """.trimIndent()
        )
        val mavenProject = projectBuilder.build(pom, buildingRequest).project

        val diktatCheckMojo = mojoRule.lookupConfiguredMojo(mavenProject, "check") as DiktatCheckMojo
        Assertions.assertEquals("diktat-analysis.yml", diktatCheckMojo.diktatConfigFile)
        Assertions.assertIterableEquals(listOf(pom.parentFile.toPath() / "src"), diktatCheckMojo.inputs.map { Path(it) })
        Assertions.assertTrue(diktatCheckMojo.excludes.isEmpty())
    }

    @Test
    fun `test plugin custom configuration`() {
        val pom = createTempFile().toFile()
        pom.writeText(
            """
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                        xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>

                    <groupId>com.saveourtool.diktat</groupId>
                    <artifactId>diktat-test</artifactId>
                    <version>1.0.0-SNAPSHOT</version>

                    <build>
                        <plugins>
                            <plugin>
                                <groupId>com.saveourtool.diktat</groupId>
                                <artifactId>diktat-maven-plugin</artifactId>
                                <configuration>
                                    <diktatConfigFile>my-diktat-config.yml</diktatConfigFile>
                                    <inputs>
                                        <input>${'$'}{project.basedir}/src/main/kotlin</input>
                                        <input>${'$'}{project.basedir}/src/test/kotlin</input>
                                    </inputs>
                                    <excludes>
                                        <exclude>${'$'}{project.basedir}/src/main/kotlin/exclusion</exclude>
                                    </excludes>
                                </configuration>
                                <executions>
                                    <execution>
                                        <goals>
                                            <goal>check</goal>
                                        </goals>
                                    </execution>
                                </executions>
                            </plugin>
                        </plugins>
                    </build>
                </project>
            """.trimIndent()
        )
        val mavenProject = projectBuilder.build(pom, buildingRequest).project

        val diktatCheckMojo = mojoRule.lookupConfiguredMojo(mavenProject, "check") as DiktatCheckMojo
        Assertions.assertEquals("my-diktat-config.yml", diktatCheckMojo.diktatConfigFile)
        Assertions.assertIterableEquals(
            listOf(pom.parentFile.toPath() / "src/main/kotlin", pom.parentFile.toPath() / "src/test/kotlin"),
            diktatCheckMojo.inputs.map { Path(it) }
        )
        Assertions.assertIterableEquals(
            listOf(pom.parentFile.toPath() / "src/main/kotlin/exclusion"),
            diktatCheckMojo.excludes.map { Path(it) }
        )
        val mojoExecutionException = Assertions.assertThrows(MojoExecutionException::class.java) {
            diktatCheckMojo.execute()
        }
        Assertions.assertEquals("Configuration file my-diktat-config.yml doesn't exist",
            mojoExecutionException.message)
    }
}
