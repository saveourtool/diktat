package org.cqfn.diktat.plugin.maven

import junit.framework.Assert
import org.apache.maven.plugin.testing.AbstractMojoTestCase
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText

/**
 * Tests for mojo configuration
 * FixMe: inject project version from outside
 * FixMe: `@Parameter` properties are not set
 * BACKTICKS_PROHIBITED is suppressed because junit 3 doesn't have @Test annotation
 */
@OptIn(ExperimentalPathApi::class)
@Suppress("TOO_LONG_FUNCTION", "BACKTICKS_PROHIBITED")
class DiktatBaseMojoTest : AbstractMojoTestCase() {
    fun `test plugin configuration`() {
        val pom = createTempFile()
        pom.writeText(
            """
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                        xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    
                    <groupId>org.cqfn.diktat</groupId>
                    <artifactId>diktat-test</artifactId>
                    <version>0.1.6-SNAPSHOT</version>
                    
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.cqfn.diktat</groupId>
                                <artifactId>diktat-maven-plugin</artifactId>
                                <version>0.1.6-SNAPSHOT</version>
                                <configuration>
                                    <diktatConfigFile>diktat-analysis.yml</diktatConfigFile>
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
        val diktatCheckMojo = lookupMojo("check", pom.toFile()) as DiktatCheckMojo
        Assert.assertEquals(false, diktatCheckMojo.debug)
        Assert.assertEquals("diktat-analysis.yml", diktatCheckMojo.diktatConfigFile)
    }
}
