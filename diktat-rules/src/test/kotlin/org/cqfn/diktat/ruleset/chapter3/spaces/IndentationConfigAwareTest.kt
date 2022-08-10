@file:Suppress("FILE_UNORDERED_IMPORTS")// False positives, see #1494.

package org.cqfn.diktat.ruleset.chapter3.spaces

import org.cqfn.diktat.ruleset.junit.NaturalDisplayName
import org.cqfn.diktat.ruleset.rules.chapter3.files.IndentationAmount.EXTENDED
import org.cqfn.diktat.ruleset.rules.chapter3.files.IndentationAmount.NONE
import org.cqfn.diktat.ruleset.rules.chapter3.files.IndentationAmount.SINGLE
import org.cqfn.diktat.ruleset.rules.chapter3.files.IndentationConfigAware.Factory.withIndentationConfig
import org.cqfn.diktat.ruleset.utils.indentation.IndentationConfig.Companion.INDENTATION_SIZE

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import org.cqfn.diktat.ruleset.chapter3.spaces.IndentationConfigFactory as IndentationConfig

@TestMethodOrder(NaturalDisplayName::class)
class IndentationConfigAwareTest {
    @ParameterizedTest(name = "$INDENTATION_SIZE = {0}")
    @ValueSource(ints = [2, 4, 8])
    fun `Int + IndentationAmount`(indentationSize: Int) {
        val config = IndentationConfig(INDENTATION_SIZE to indentationSize)

        withIndentationConfig(config) {
            assertThat(42 + NONE).isEqualTo(42)
            assertThat(42 + SINGLE).isEqualTo(42 + indentationSize)
            assertThat(42 + EXTENDED).isEqualTo(42 + 2 * indentationSize)
        }
    }

    @ParameterizedTest(name = "$INDENTATION_SIZE = {0}")
    @ValueSource(ints = [2, 4, 8])
    fun `Int - IndentationAmount`(indentationSize: Int) {
        val config = IndentationConfig(INDENTATION_SIZE to indentationSize)

        withIndentationConfig(config) {
            assertThat(42 - NONE).isEqualTo(42)
            assertThat(42 - SINGLE).isEqualTo(42 - indentationSize)
            assertThat(42 - EXTENDED).isEqualTo(42 - 2 * indentationSize)
        }
    }

    @ParameterizedTest(name = "$INDENTATION_SIZE = {0}")
    @ValueSource(ints = [2, 4, 8])
    fun `IndentationAmount + Int`(indentationSize: Int) {
        val config = IndentationConfig(INDENTATION_SIZE to indentationSize)

        withIndentationConfig(config) {
            assertThat(NONE + 42).isEqualTo(42 + NONE)
            assertThat(SINGLE + 42).isEqualTo(42 + SINGLE)
            assertThat(EXTENDED + 42).isEqualTo(42 + EXTENDED)

            assertThat(42 + (SINGLE + 2)).isEqualTo((42 + SINGLE) + 2)
        }
    }

    @ParameterizedTest(name = "$INDENTATION_SIZE = {0}")
    @ValueSource(ints = [2, 4, 8])
    fun `IndentationAmount - Int`(indentationSize: Int) {
        val config = IndentationConfig(INDENTATION_SIZE to indentationSize)

        withIndentationConfig(config) {
            assertThat(NONE - 42).isEqualTo(-(42 - NONE))
            assertThat(SINGLE - 42).isEqualTo(-(42 - SINGLE))
            assertThat(EXTENDED - 42).isEqualTo(-(42 - EXTENDED))

            assertThat(42 - (SINGLE - 2)).isEqualTo(42 - SINGLE + 2)
        }
    }

    @ParameterizedTest(name = "$INDENTATION_SIZE = {0}")
    @ValueSource(ints = [2, 4, 8])
    fun `IndentationAmount + IndentationAmount`(indentationSize: Int) {
        val config = IndentationConfig(INDENTATION_SIZE to indentationSize)

        withIndentationConfig(config) {
            assertThat(NONE + SINGLE).isEqualTo(0 + SINGLE)
            assertThat(SINGLE + SINGLE).isEqualTo(0 + EXTENDED)

            assertThat(42 + SINGLE + SINGLE).isEqualTo(42 + EXTENDED)
            assertThat(42 + (SINGLE + SINGLE)).isEqualTo(42 + EXTENDED)
        }
    }

    @ParameterizedTest(name = "$INDENTATION_SIZE = {0}")
    @ValueSource(ints = [2, 4, 8])
    fun `IndentationAmount - IndentationAmount`(indentationSize: Int) {
        val config = IndentationConfig(INDENTATION_SIZE to indentationSize)

        withIndentationConfig(config) {
            assertThat(NONE - SINGLE).isEqualTo(0 - SINGLE)
            assertThat(SINGLE - SINGLE).isEqualTo(0)
            assertThat(EXTENDED - SINGLE).isEqualTo(0 + SINGLE)
            assertThat(NONE - EXTENDED).isEqualTo(0 - EXTENDED)

            assertThat(42 + (SINGLE - SINGLE)).isEqualTo(42 + SINGLE - SINGLE)
        }
    }

    @ParameterizedTest(name = "$INDENTATION_SIZE = {0}")
    @ValueSource(ints = [2, 4, 8])
    fun unaryPlus(indentationSize: Int) {
        val config = IndentationConfig(INDENTATION_SIZE to indentationSize)

        withIndentationConfig(config) {
            assertThat(+NONE).isEqualTo(0)
            assertThat(+SINGLE).isEqualTo(indentationSize)
            assertThat(+EXTENDED).isEqualTo(2 * indentationSize)

            assertThat(EXTENDED - SINGLE).isEqualTo(+SINGLE)
        }
    }

    @ParameterizedTest(name = "$INDENTATION_SIZE = {0}")
    @ValueSource(ints = [2, 4, 8])
    fun unaryMinus(indentationSize: Int) {
        val config = IndentationConfig(INDENTATION_SIZE to indentationSize)

        withIndentationConfig(config) {
            assertThat(-NONE).isEqualTo(0)
            assertThat(-SINGLE).isEqualTo(-indentationSize)
            assertThat(-EXTENDED).isEqualTo(-2 * indentationSize)

            assertThat(NONE - SINGLE).isEqualTo(-SINGLE)
            assertThat(NONE - EXTENDED).isEqualTo(-EXTENDED)
        }
    }
}
