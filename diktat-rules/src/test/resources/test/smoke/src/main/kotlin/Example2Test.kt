package test.smoke

import java.io.IOException
import java.util.Properties
import kotlin.system.exitProcess
import org.slf4j.LoggerFactory

data @ExperimentalStdlibApi public class Example(val foo:Int, val bar:Double):SuperExample("lorem ipsum")

private class Test : Exception()
/*    this class is unused */
//private class Test : RuntimeException()

/**
 * Creates a docker container with [file], prepared to execute it
 *
 * @param runConfiguration a [RunConfiguration] for the supplied binary
 * @param file a file that will be included as an executable
 * @param resources additional resources
 * @throws DockerException if docker daemon has returned an error
 * @throws DockerException if docker daemon has returned an error
 * @throws RuntimeException if an exception not specific to docker has occurred
 * @return id of created container or null if it wasn't created
 */
internal fun createWithFile(runConfiguration: RunConfiguration,
                            containerName: String,
                            file: File,
                            resources: Collection<File> = emptySet()) {}

private fun foo (node: ASTNode) {
    when (node.elementType) {
        CLASS, FUN, PRIMARY_CONSTRUCTOR, SECONDARY_CONSTRUCTOR -> checkAnnotation(node)
    }
    val qwe = a
            && b
    val qwe = a &&
            b

    if (x) // comment
        foo()

    setOf<Object>(IOException(), Properties(), LoggerFactory())
}
