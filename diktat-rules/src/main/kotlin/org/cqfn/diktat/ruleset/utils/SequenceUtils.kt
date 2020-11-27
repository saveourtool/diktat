package org.cqfn.diktat.ruleset.utils

/**
 * @param pred a predicate
 * @return filtered sequence
 */
fun <T> Sequence<T>.takeWhileInclusive(pred: (T) -> Boolean): Sequence<T> {
    var shouldContinue = true
    return takeWhile {
        val result = shouldContinue
        shouldContinue = pred(it)
        result
    }
}
