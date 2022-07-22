package org.cqfn.diktat.ruleset.chapter3.spaces

import org.cqfn.diktat.ruleset.chapter3.spaces.IndentationRuleTestMixin.IndentationConfig
import org.cqfn.diktat.ruleset.rules.chapter3.files.IndentationAmount.EXTENDED
import org.cqfn.diktat.ruleset.rules.chapter3.files.IndentationAmount.NONE
import org.cqfn.diktat.ruleset.rules.chapter3.files.IndentationAmount.SINGLE
import org.cqfn.diktat.ruleset.rules.chapter3.files.IndentationConfigAware.Factory.withIndentationConfig

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.MethodOrderer.DisplayName
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@TestMethodOrder(DisplayName::class)
class IndentationConfigAwareTest {
    @ParameterizedTest(name = "indentationSize = {0}")
    @ValueSource(ints = [2, 4, 8])
    fun `Int + IndentationAmount`(indentationSize: Int) {
        val config = IndentationConfig("indentationSize" to indentationSize)

        withIndentationConfig(config) {
            assertThat(42 + NONE).isEqualTo(42)
            assertThat(42 + SINGLE).isEqualTo(42 + indentationSize)
            assertThat(42 + EXTENDED).isEqualTo(42 + 2 * indentationSize)
        }
    }

    @ParameterizedTest(name = "indentationSize = {0}")
    @ValueSource(ints = [2, 4, 8])
    fun `Int - IndentationAmount`(indentationSize: Int) {
        val config = IndentationConfig("indentationSize" to indentationSize)

        withIndentationConfig(config) {
            assertThat(42 - NONE).isEqualTo(42)
            assertThat(42 - SINGLE).isEqualTo(42 - indentationSize)
            assertThat(42 - EXTENDED).isEqualTo(42 - 2 * indentationSize)
        }
    }

    @ParameterizedTest(name = "indentationSize = {0}")
    @ValueSource(ints = [2, 4, 8])
    fun `IndentationAmount + Int`(indentationSize: Int) {
        val config = IndentationConfig("indentationSize" to indentationSize)

        withIndentationConfig(config) {
            assertThat(NONE + 42).isEqualTo(42 + NONE)
            assertThat(SINGLE + 42).isEqualTo(42 + SINGLE)
            assertThat(EXTENDED + 42).isEqualTo(42 + EXTENDED)

            assertThat(42 + (SINGLE + 2)).isEqualTo((42 + SINGLE) + 2)
        }
    }

    @ParameterizedTest(name = "indentationSize = {0}")
    @ValueSource(ints = [2, 4, 8])
    fun `IndentationAmount - Int`(indentationSize: Int) {
        val config = IndentationConfig("indentationSize" to indentationSize)

        withIndentationConfig(config) {
            assertThat(NONE - 42).isEqualTo(-(42 - NONE))
            assertThat(SINGLE - 42).isEqualTo(-(42 - SINGLE))
            assertThat(EXTENDED - 42).isEqualTo(-(42 - EXTENDED))

            assertThat(42 - (SINGLE - 2)).isEqualTo(42 - SINGLE + 2)
        }
    }

    @ParameterizedTest(name = "indentationSize = {0}")
    @ValueSource(ints = [2, 4, 8])
    fun `IndentationAmount + IndentationAmount`(indentationSize: Int) {
        val config = IndentationConfig("indentationSize" to indentationSize)

        withIndentationConfig(config) {
            assertThat(NONE + SINGLE).isEqualTo(0 + SINGLE)
            assertThat(SINGLE + SINGLE).isEqualTo(0 + EXTENDED)

            assertThat(42 + SINGLE + SINGLE).isEqualTo(42 + EXTENDED)
            assertThat(42 + (SINGLE + SINGLE)).isEqualTo(42 + EXTENDED)
        }
    }

    @ParameterizedTest(name = "indentationSize = {0}")
    @ValueSource(ints = [2, 4, 8])
    fun `IndentationAmount - IndentationAmount`(indentationSize: Int) {
        val config = IndentationConfig("indentationSize" to indentationSize)

        withIndentationConfig(config) {
            assertThat(NONE - SINGLE).isEqualTo(0 - SINGLE)
            assertThat(SINGLE - SINGLE).isEqualTo(0)
            assertThat(EXTENDED - SINGLE).isEqualTo(0 + SINGLE)
            assertThat(NONE - EXTENDED).isEqualTo(0 - EXTENDED)

            assertThat(42 + (SINGLE - SINGLE)).isEqualTo(42 + SINGLE - SINGLE)
        }
    }

    @ParameterizedTest(name = "indentationSize = {0}")
    @ValueSource(ints = [2, 4, 8])
    fun unaryPlus(indentationSize: Int) {
        val config = IndentationConfig("indentationSize" to indentationSize)

        withIndentationConfig(config) {
            assertThat(+NONE).isEqualTo(0)
            assertThat(+SINGLE).isEqualTo(indentationSize)
            assertThat(+EXTENDED).isEqualTo(2 * indentationSize)

            assertThat(EXTENDED - SINGLE).isEqualTo(+SINGLE)
        }
    }

    @ParameterizedTest(name = "indentationSize = {0}")
    @ValueSource(ints = [2, 4, 8])
    fun unaryMinus(indentationSize: Int) {
        val config = IndentationConfig("indentationSize" to indentationSize)

        withIndentationConfig(config) {
            assertThat(-NONE).isEqualTo(0)
            assertThat(-SINGLE).isEqualTo(-indentationSize)
            assertThat(-EXTENDED).isEqualTo(-2 * indentationSize)

            assertThat(NONE - SINGLE).isEqualTo(-SINGLE)
            assertThat(NONE - EXTENDED).isEqualTo(-EXTENDED)
        }
    }
}
