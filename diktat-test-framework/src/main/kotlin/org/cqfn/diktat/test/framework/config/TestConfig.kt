package org.cqfn.diktat.test.framework.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * This class is used to serialize/deserialize json representation
 * that is used to store command line arguments
 * @property executionCommand command line execution command, use shell like "cmd", "bash" or other
 * @property expectedResultFile expected result file can be a full path or a relative path to a resource
 * @property testFile testFile can be a full path or a relative path to a resource
 * @property executionType executionType that  controls processing of the test (like COMPARE, MIXED, CHECK_WARN, e.t.c)
 * @property testProfile
 * @property inPlace option that controls if changes and automatic fix should be done directly in file
 */
@Serializable
@Suppress("ForbiddenComment")
class TestConfig internal constructor(
    val executionCommand: String,
    val expectedResultFile: String,
    val testFile: String,
    val executionType: ExecutionType,
    @SerialName("profile") val testProfile: TestProfile,
    val inPlace: Boolean = false
) {
    /**
     * test name - it is not included in config content, but is injected on runtime by setter
     */
    var testName: String? = null
        private set

    // FixMe: not used by for now, fix the description when the class will be ready
    override fun toString() =
            """(executionCommand: $executionCommand, expectedResultFile: $expectedResultFile, inPlace: $inPlace,
                    executionType: $executionCommand)"""

    /**
     * @param testName
     * @return [TestConfig] with updated [testName]
     */
    fun setTestName(testName: String): TestConfig {
        this.testName = testName
        return this
    }

    /**
     * Different modes of tests execution
     */
    enum class ExecutionType {
        CHECK_WARN, COMPARE, MIXED
    }

    /**
     * different profiles that can be used to control common processing part for tests
     * (processing differs for different programming languages)
     */
    @Suppress("UNUSED")
    enum class TestProfile {
        CXX, JAVA, KT, PYTHON
    }
}
