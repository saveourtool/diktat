package org.cqfn.diktat.test.framework.config

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * This class is used to serialize/deserialize json representation
 * that is used to store command line arguments
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class TestConfig @JsonCreator internal constructor(
        // command line execution command, use shell like "cmd", "bash" or other
        @param:JsonProperty("executionCommand") val executionCommand: String,
        // expected result file can be a full path or a relative path to a resource
        @param:JsonProperty("expectedResultFile") val expectedResultFile: String,
        // testFile can be a full path or a relative path to a resource
        @param:JsonProperty("testFile") val testFile: String,
        // executionType that  controls processing of the test (like COMPARE, MIXED, CHECK_WARN, e.t.c)
        @param:JsonProperty("executionType") val executionType: ExecutionType,
        @param:JsonProperty("profile") val testProfile: TestProfile,
        // option that controls if changes and automatic fix should be done directly in file
        @param:JsonProperty("inPlace", defaultValue = "false") val inPlace: Boolean
) {
    enum class ExecutionType {
        COMPARE, CHECK_WARN, MIXED
    }

    // different profiles that can be used to control common processing part for tests
// (processing differs for different programming languages)
    enum class TestProfile {
        CXX, PYTHON, JAVA, KT
    }

    // testName - it is not included in config content, but is injected on runtime by setter
    var testName: String? = null
        private set

    // FixMe: not used by for now, fix the description when the class will be ready
    override fun toString(): String {
        return """(executionCommand: $executionCommand, expectedResultFile: $expectedResultFile, inPlace: $inPlace,
                executionType: $executionCommand)"""
    }

    fun setTestName(testName: String?): TestConfig {
        this.testName = testName
        return this
    }

}
