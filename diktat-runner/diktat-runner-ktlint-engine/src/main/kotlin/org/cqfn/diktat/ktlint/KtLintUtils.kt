/**
 * This file contains util methods for __KtLint__
 */

package org.cqfn.diktat.ktlint

import java.nio.file.Path
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.relativeTo

/**
 * @param sourceRootDir
 * @return relative path to [sourceRootDir] as [String]
 */
fun Path.relativePathStringTo(sourceRootDir: Path): String = relativeTo(sourceRootDir).invariantSeparatorsPathString
