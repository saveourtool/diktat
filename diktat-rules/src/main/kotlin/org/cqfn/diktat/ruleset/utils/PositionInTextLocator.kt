package org.cqfn.diktat.ruleset.utils

//fixme this code is copy-pasted from ktlint. Change it
internal typealias LineAndColumn = Pair<Int, Int>

/**
 * Calculate position in text - line and column based on offset from the text start.
 */
internal fun buildPositionInTextLocator(text: String): (offset: Int) -> LineAndColumn {
    val textLength = text.length
    val identifierArray = ArrayList<Int>()
    var endOfLineIndex = -1

    do {
        identifierArray.add(endOfLineIndex + 1)
        endOfLineIndex = text.indexOf('\n', endOfLineIndex + 1)
    } while (endOfLineIndex != -1)

    identifierArray.add(textLength + if (identifierArray.last() == textLength) 1 else 0)

    val segmentTree = SegmentTree(identifierArray.toIntArray())

    return { offset ->
        val line = segmentTree.indexOf(offset)
        if (line != -1) {
            val column = offset - segmentTree.get(line).left
            line + 1 to column + 1
        } else {
            1 to 1
        }
    }
}

private class SegmentTree(sortedArray: IntArray) {

    init {
        require(sortedArray.size > 1) { "At least two data points are required" }
        sortedArray.reduce { current, next ->
            require(current <= next) { "Data points are not sorted (ASC)" }
            next
        }
    }

    private val segments: List<Segment> = sortedArray
            .dropLast(1)
            .mapIndexed { index: Int, element: Int ->
                Segment(element, sortedArray[index + 1] - 1)
            }

    fun get(index: Int): Segment = segments[index]

    fun indexOf(index: Int): Int = binarySearch(index, 0, segments.size - 1)

    private fun binarySearch(compareElement: Int, left: Int, right: Int): Int = when {
        left > right -> -1
        else -> {
            val index = left + (right - left) / 2
            val midElement = segments[index]
            if (compareElement < midElement.left) {
                binarySearch(compareElement, left, index - 1)
            } else {
                if (midElement.right < compareElement) binarySearch(compareElement, index + 1, right) else index
            }
        }
    }
}

private data class Segment(
        val left: Int,
        val right: Int
)
