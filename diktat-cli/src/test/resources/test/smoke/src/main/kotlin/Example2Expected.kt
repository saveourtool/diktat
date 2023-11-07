// ;warn:$line:1: [HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE] files that contain multiple or no classes should contain description of what is inside of this file: there are 2 declared classes and/or objects (cannot be auto-corrected){{.*}}
package com.saveourtool.diktat

import org.slf4j.LoggerFactory

import java.io.IOException
import java.util.Properties

/**
 * @property foo
 * @property bar
 */
@ExperimentalStdlibApi public data class Example(val foo: Int, val bar: Double) : SuperExample("lorem ipsum")

// ;warn:36:3: [EMPTY_BLOCK_STRUCTURE_ERROR] incorrect format of empty block: empty blocks are forbidden unless it is function with override keyword (cannot be auto-corrected){{.*}}
private class TestException : Exception()
/* this class is unused */
// private class Test : RuntimeException()

/**
 * Creates a docker container with [file], prepared to execute it
 *
 * @param runConfiguration a [RunConfiguration] for the supplied binary
 * @param file a file that will be included as an executable
 * @param resources additional resources
 * @param containerName
 * @return id of created container or null if it wasn't created
 * @throws DockerException if docker daemon has returned an error
 * @throws DockerException if docker daemon has returned an error
 * @throws RuntimeException if an exception not specific to docker has occurred
 */
internal fun createWithFile(runConfiguration: RunConfiguration,
                            containerName: String,
                            file: File,
                            resources: Collection<File> = emptySet()
) {}

private fun foo(node: ASTNode) {
    when (node.elementType) {
        CLASS, FUN, PRIMARY_CONSTRUCTOR, SECONDARY_CONSTRUCTOR -> checkAnnotation(node)
    }
    val qwe = a && b
    val qwe = a &&
            b

    // comment
    if (x) {
        foo()
    }

    setOf<Object>(IOException(), Properties(), LoggerFactory())
}
