package org.cqfn.diktat.plugin.maven

import com.soebes.itf.jupiter.extension.MavenGoal
import com.soebes.itf.jupiter.extension.MavenJupiterExtension
import com.soebes.itf.jupiter.extension.MavenTest
import com.soebes.itf.jupiter.maven.MavenExecutionResult
import org.junit.jupiter.api.Assertions
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.readText

/**
 * Integration tests for diktat-maven-plugin.
 * Run against the project from diktat-examples.
 */
@OptIn(ExperimentalPathApi::class)
@MavenJupiterExtension
class DiktatMavenPluginIntegrationTest {
    @MavenTest
    @MavenGoal("diktat:check@diktat")
    fun maven(result: MavenExecutionResult) {
        val mavenLog = result.mavenLog.stdout.readText()
        println(mavenLog)

        Assertions.assertEquals(1, result.returnCode)
        Assertions.assertFalse(result.isError)
        Assertions.assertFalse(result.isSuccesful)
        Assertions.assertTrue(result.isFailure)

        Assertions.assertTrue(
            mavenLog.contains("[HEADER_MISSING_OR_WRONG_COPYRIGHT]")
        )
    }
}
