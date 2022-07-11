package org.cqfn.diktat.ruleset.smoke

class DiktatSaveSmokeTest : DiktatSmokeTestBase() {
    override fun fixAndCompareBase(
        config: String,
        test: String,
        expected: String
    ) {
        saveSmokeTest(config, test, expected)
    }
}
