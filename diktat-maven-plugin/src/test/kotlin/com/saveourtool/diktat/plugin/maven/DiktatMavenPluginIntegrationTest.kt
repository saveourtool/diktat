package com.saveourtool.diktat.plugin.maven

import com.soebes.itf.jupiter.extension.MavenGoal
import com.soebes.itf.jupiter.extension.MavenJupiterExtension
import com.soebes.itf.jupiter.extension.MavenTest
import com.soebes.itf.jupiter.maven.MavenExecutionResult
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.div
import kotlin.io.path.readText

/**
 * Integration tests for diktat-maven-plugin. Run against the project from diktat-examples.
 * The whole pipeline is as follows:
 * * For each test case, test data is copied from examples with respect to maven-itf requirements, .mvn/jvm.config and .mvn/maven.config are copied, too.
 *   **Note**: for maven-itf-plugin, test name should equal example project's directory name, which we have in `pom.xml`.
 * * maven-failsafe-plugin launches tests; for each test case a separate maven process is spawned. `diktat.version` is taken from `.mvn/maven.config`
 *   and the exact value is written when maven copies resources.
 * * maven execution results are analyzed here; `.mvn/jvm.config` is used to attach jacoco java agent to every maven process and generate individual execution reports.
 *
 * When within an IDE (e.g.: _IDEA_), don't run this test directly: it's
 * expected to be executed from a forked JVM. Instead, run a `verify` _lifecycle
 * phase_ for the `diktat-maven-plugin` submodule, as if you were running
 *
 * ```console
 * $ mvn -pl diktat-maven-plugin verify
 * ```
 *
 * from your terminal. If multiple JDKs are installed, be sure to pass
 * `JAVA_HOME` to the _run configuration_, so that the parent and the forked
 * JVMs have the same version.
 */
@MavenJupiterExtension
class DiktatMavenPluginIntegrationTest {
    @BeforeEach
    fun beforeEach(testInfo: TestInfo) {
        val method = testInfo.testMethod.orElse(null) ?: return

        (Path("target") / "jacoco-it-${method.name}.exec").deleteIfExists()
    }

    @MavenTest
    @MavenGoal("diktat:check@diktat")
    fun diktatCheck(testInfo: TestInfo, result: MavenExecutionResult) {
        Assertions.assertEquals(1, result.returnCode)
        Assertions.assertFalse(result.isSuccessful)
        Assertions.assertTrue(result.isFailure)

        val mavenLog = result.mavenLog.stdout.readText()

        assertThat(mavenLog).contains("[FILE_NAME_MATCH_CLASS]")

        val method = testInfo.testMethod.get()
        File(result.mavenProjectResult.targetProjectDirectory.toFile(), "target/jacoco-it.exec").copyTo(
            File("target/jacoco-it-${method.name}.exec")
        )
    }

    @MavenTest
    @MavenGoal("diktat:fix@diktat")
    fun diktatFix(testInfo: TestInfo, result: MavenExecutionResult) {
        Assertions.assertEquals(1, result.returnCode)
        Assertions.assertFalse(result.isSuccessful)
        Assertions.assertTrue(result.isFailure)

        val mavenLog = result.mavenLog.stdout.readText()

        with(SoftAssertions()) {
            try {
                assertThat(mavenLog).containsPattern("""Original and formatted content differ, writing to [:\w/\\-]+Test\.kt\.\.\.""")
                assertThat(mavenLog).containsPattern("There are \\d+ lint errors")
                assertThat(mavenLog).contains("[MISSING_KDOC_TOP_LEVEL]")
            } finally {
                assertAll()
            }
        }

        val method = testInfo.testMethod.get()
        File(result.mavenProjectResult.targetProjectDirectory.toFile(), "target/jacoco-it.exec").copyTo(
            File("target/jacoco-it-${method.name}.exec")
        )
    }
}
