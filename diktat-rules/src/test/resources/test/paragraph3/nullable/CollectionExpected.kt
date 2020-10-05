package test.paragraph3.nullable

import java.util.*

class A {
    val a: List<Int> = emptyList()
    val b: Iterable<Int> = emptyList()
    val c: Map<Int, Int> = emptyMap()
    val d: Array<Int> = emptyArray()
    val e: Set<Int> = emptySet()
    val f: Sequence<Int> = emptySequence()
    val g: MutableList<Int> = mutableListOf()
    val h: MutableMap<Int, Int> = mutableMapOf()
    val i: MutableSet<Int> = mutableSetOf()
    val j: LinkedList<Int> = LinkedList()
    val k: LinkedHashMap<Int, Int> = LinkedHashMap()
    val l: LinkedHashSet<Int> = LinkedHashSet()
    val m: Queue<Int> = LinkedList()
    val s: Iterator<Int>? = null
}
