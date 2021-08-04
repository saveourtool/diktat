package org.cqfn.diktat.plugin.maven

import com.soebes.itf.jupiter.extension.MavenGoal
import com.soebes.itf.jupiter.extension.MavenJupiterExtension
import com.soebes.itf.jupiter.extension.MavenTest
import com.soebes.itf.jupiter.maven.MavenExecutionResult
import org.junit.jupiter.api.Assertions
import java.io.File
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.readText

/**
 * Integration tests for diktat-maven-plugin. Run against the project from diktat-examples.
 * The whole pipeline is as follows:
 * * For each test case, test data is copied from examples with respect to maven-itf requirements, .mvn/jvm.config and .mvn/maven.config are copied too
 *   Note: for maven itf test name should equal example project's directory name, which we have in pom.xml.
 * * maven-failsafe-plugin launches tests; for each test case a separate maven process is spawned. diktat.version is taken from .mvn/maven.config
 *   and the exact value is written when maven copies resources.
 * * maven execution results are analyzed here; .mvn/jvm.config is used to attach jacoco java agent to every maven process and generate individual execution reports
 */
@OptIn(ExperimentalPathApi::class)
@MavenJupiterExtension
class DiktatMavenPluginIntegrationTest {
    @MavenTest
    @MavenGoal("diktat:check@diktat")
    fun diktatCheck(result: MavenExecutionResult) {
        Assertions.assertEquals(1, result.returnCode)
        Assertions.assertFalse(result.isSuccessful)
        Assertions.assertTrue(result.isFailure)

        val mavenLog = result.mavenLog.stdout.readText()
        Assertions.assertTrue(
            mavenLog.contains("[FILE_NAME_MATCH_CLASS]")
        )

        File(result.mavenProjectResult.targetProjectDirectory, "target/jacoco-it.exec").copyTo(
            File("target/jacoco-it-1.exec")
        )
    }

    @MavenTest
    @MavenGoal("diktat:fix@diktat")
    fun diktatFix(result: MavenExecutionResult) {
        Assertions.assertEquals(1, result.returnCode)
        Assertions.assertFalse(result.isSuccessful)
        Assertions.assertTrue(result.isFailure)

        val mavenLog = result.mavenLog.stdout.readText()
        Assertions.assertTrue(
            mavenLog.contains("Original and formatted content differ, writing to Test.kt...")
        )
        Assertions.assertTrue(
            mavenLog.contains(Regex("There are \\d+ lint errors"))
        )
        Assertions.assertTrue(
            mavenLog.contains("[MISSING_KDOC_TOP_LEVEL]")
        )

        File(result.mavenProjectResult.targetProjectDirectory, "target/jacoco-it.exec").copyTo(
            File("target/jacoco-it-2.exec")
        )
    }
}
