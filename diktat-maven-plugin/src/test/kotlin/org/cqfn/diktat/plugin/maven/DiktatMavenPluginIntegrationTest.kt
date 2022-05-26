package org.cqfn.diktat.plugin.maven

import com.soebes.itf.jupiter.extension.MavenGoal
import com.soebes.itf.jupiter.extension.MavenJupiterExtension
import com.soebes.itf.jupiter.extension.MavenTest
import com.soebes.itf.jupiter.maven.MavenExecutionResult
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo
import java.io.File
import kotlin.io.path.ExperimentalPathApi
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
@OptIn(ExperimentalPathApi::class)
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

        mavenLog.assertContains("[FILE_NAME_MATCH_CLASS]")

        val method = testInfo.testMethod.get()
        File(result.mavenProjectResult.targetProjectDirectory, "target/jacoco-it.exec").copyTo(
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

        mavenLog.assertContains(Regex("""Original and formatted content differ, writing to [:\w/\\]+Test\.kt\.\.\."""))
        mavenLog.assertContains(Regex("There are \\d+ lint errors"))
        mavenLog.assertContains("[MISSING_KDOC_TOP_LEVEL]")

        val method = testInfo.testMethod.get()
        File(result.mavenProjectResult.targetProjectDirectory, "target/jacoco-it.exec").copyTo(
            File("target/jacoco-it-${method.name}.exec")
        )
    }

    /**
     * Asserts that this string contains a [substring][other].
     *
     * @param other the expected substring.
     */
    private inline fun String.assertContains(other: CharSequence,
                                             crossinline lazyMessage: () -> String = {
            "The string: \"$this\" doesn't contain the substring: \"$other\""
        }) {
        Assertions.assertTrue(contains(other)) {
            lazyMessage()
        }
    }

    /**
     * Asserts that this string contains a substring matching the [regex].
     */
    private inline fun String.assertContains(regex: Regex,
                                             crossinline lazyMessage: () -> String = {
            "The string: \"$this\" doesn't contain any substring matching the regex: \"$regex\""
        }) {
        Assertions.assertTrue(contains(regex)) {
            lazyMessage()
        }
    }
}
