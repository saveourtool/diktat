package org.cqfn.diktat.plugin.gradle

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class UtilsTest {
    @Test
    fun `test gradle version`() {
        Assertions.assertEquals(
            GradleVersion.fromString("6.6.1"),
            GradleVersion(6, 6, 1, null)
        )

        Assertions.assertEquals(
            GradleVersion.fromString("6.7"),
            GradleVersion(6, 7, 0, null)
        )

        Assertions.assertEquals(
            GradleVersion.fromString("6.7-rc-5"),
            GradleVersion(6, 7, 0, "rc-5")
        )
    }
}
