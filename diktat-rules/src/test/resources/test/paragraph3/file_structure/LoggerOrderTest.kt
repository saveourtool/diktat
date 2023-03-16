package test.paragraph3.file_structure

import org.junit.platform.commons.logging.LoggerFactory

class LoggerOrderExample {
    private val logger = getLogger("string template")
    private val a = "a"
    private val logger = getLogger("""string template""")
    private val b = "b"
    private val logger = getLogger("""
string template
""")
    private val c = "c"
    private val logger = getLogger(this.javaClass)
    private val d = "d"
    private val logger = getLogger(this.javaClass.name)
    private val e = "e"
    private val logger = getLogger(javaClass)
    private val f = "f"
    private val logger = getLogger(javaClass.name)
    private val g = "g"
    private val logger = getLogger(Foo::class.java)
    private val h = "h"
    private val logger = getLogger(Foo::class.java.name)
    private val i = "i"
    private val logger = getLogger(Foo::class)
    private val j = "j"
    private val logger = getLogger(Foo::class.name)
    private val k = "k"
    private val logger = getLogger<Foo>()
    private val l = "l"
    private val logger = getLogger({}.javaClass)
    private val m = "m"
    private val logger = LoggerFactory.getLogger(m)
    private val n = "n"
    private val logger = n.getLogger()
    private val o = "o"
    private val logger = o { get() }
    private val p = "p"
    private val logger = p::class.java.enclosingClass
    private val q = "q"
    private val logger = q::class.java
    private val r = "r"
    private val logger = r::javaClass
    private val s = "s"
    private val t = "t"
    private val logger = getLogger("$s$t")
    private val u = "u"
    private val v = "v"
    private val w = "w"
    private val x = "x"
    private val y = "y"
    private val z = "z"
    private val logger = getLogger("$s$t$u$v$w") { x + y + z }
}
