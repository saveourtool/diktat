package com.saveourtool.diktat.ruleset.junit

import org.junit.jupiter.api.MethodOrderer.DisplayName
import org.junit.jupiter.api.MethodOrdererContext

/**
 * Like [DisplayName], but uses the _natural sort_ order
 * (i. e. `test 9` < `test 10`).
 *
 * @see DisplayName
 */
class NaturalDisplayName : DisplayName() {
    /**
     * Sort the methods encapsulated in the supplied [MethodOrdererContext]
     * alphanumerically based on their display names.
     */
    override fun orderMethods(context: MethodOrdererContext) =
        context.methodDescriptors.sortWith { left, right ->
            val leftDisplayName = left.displayName
            val rightDisplayName = right.displayName

            val leftArgs = callArguments.find(leftDisplayName)
                ?.groups
                ?.get("args")
                ?.value
            val rightArgs = callArguments.find(rightDisplayName)
                ?.groups
                ?.get("args")
                ?.value

            /*
             * If two methods are invoked with the same arguments, exclude the
             * arguments from the comparison, i. e. order `foo()` before `foobar()`.
             */
            val (leftName, rightName) = when {
                leftArgs != null && leftArgs == rightArgs -> {
                    val withoutArgs: String.() -> String = {
                        substring(0, length - leftArgs.length - 2)
                    }

                    leftDisplayName.withoutArgs() to rightDisplayName.withoutArgs()
                }

                else -> leftDisplayName to rightDisplayName
            }

            naturalComparator(leftName)(rightName)
        }

    private companion object {
        private val callArguments = Regex("""\((?<args>[^()]*)\)$""")

        /**
         * For "case 1" and "case 10", returns "case " as the common prefix.
         */
        @Suppress("TYPE_ALIAS")
        private val commonNonNumericPrefix: (String) -> (String) -> String = { left ->
            { right ->
                left.commonPrefixWith(right).takeWhile(isNotDigit)
            }
        }

        /**
         * Returns `true` if this `Char` is not a digit, `false` otherwise.
         */
        private val isNotDigit: Char.() -> Boolean = {
            !isDigit()
        }

        /**
         * Parses this string as an [Int] number and returns the result. Returns
         * `null` if the string is not a valid representation of a number.
         */
        private val asIntOrNull: String.() -> Int? = {
            when {
                isEmpty() -> null
                else -> try {
                    toInt()
                } catch (_: NumberFormatException) {
                    null
                }
            }
        }

        private fun naturalComparator(left: String): (String) -> Int = { right ->
            val commonNonNumericPrefix = commonNonNumericPrefix(left)(right)

            val numericInfix: String.() -> String = {
                val tail = subSequence(commonNonNumericPrefix.length, length)

                tail.takeWhile(Char::isDigit).toString()
            }

            val leftInfixRaw = left.numericInfix()
            val rightInfixRaw = right.numericInfix()

            val leftInfix = leftInfixRaw.asIntOrNull()
            val rightInfix = rightInfixRaw.asIntOrNull()

            when {
                leftInfix != null && rightInfix != null -> when (leftInfix) {
                    /*
                     * When infixes are equal, recursively compare the
                     * remainder suffixes. Thus, "foo9Bar" < "foo9bar".
                     */
                    rightInfix -> {
                        val leftSuffix = left.substring(commonNonNumericPrefix.length + leftInfixRaw.length)
                        val rightSuffix = right.substring(commonNonNumericPrefix.length + rightInfixRaw.length)
                        naturalComparator(leftSuffix)(rightSuffix)
                    }

                    /*
                     * "foo9bar" < "foo10bar".
                     */
                    else -> leftInfix - rightInfix
                }

                else -> left.compareTo(right)
            }
        }
    }
}
