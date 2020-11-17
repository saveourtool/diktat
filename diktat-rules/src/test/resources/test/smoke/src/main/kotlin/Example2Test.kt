package test.smoke

import java.io.IOException
import java.util.Properties
import kotlin.system.exitProcess
import org.slf4j.LoggerFactory

data @ExperimentalStdlibApi public class Example(val foo:Int, val bar:Double):SuperExample("lorem ipsum")

private class Test : Exception()
/*    this class is unused */
//private class Test : RuntimeException()

private fun foo (node: ASTNode) {
    when (node.elementType) {
        CLASS, FUN, PRIMARY_CONSTRUCTOR, SECONDARY_CONSTRUCTOR -> checkAnnotation(node)
    }
}