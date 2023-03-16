package test.paragraph3.file_structure

import org.junit.platform.commons.logging.LoggerFactory

class LoggerOrderExample {
    private val logger = getLogger("string template")
    private val logger = getLogger("""string template""")
    private val logger = getLogger("""
string template
""")
    private val logger = getLogger(this.javaClass)
    private val logger = getLogger(this.javaClass.name)
    private val logger = getLogger(javaClass)
    private val logger = getLogger(javaClass.name)
    private val logger = getLogger(Foo::class.java)
    private val logger = getLogger(Foo::class.java.name)
    private val logger = getLogger(Foo::class)
    private val logger = getLogger(Foo::class.name)
    private val logger = getLogger<Foo>()
    private val logger = getLogger({}.javaClass)
    private val a = "a"
    private val b = "b"
    private val c = "c"
    private val d = "d"
    private val e = "e"
    private val f = "f"
    private val g = "g"
    private val h = "h"
    private val i = "i"
    private val j = "j"
    private val k = "k"
    private val l = "l"
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
