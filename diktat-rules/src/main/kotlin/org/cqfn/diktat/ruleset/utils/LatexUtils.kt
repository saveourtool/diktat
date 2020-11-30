package org.cqfn.diktat.ruleset.utils

import java.io.PrintWriter

fun PrintWriter.writeln(text: String) {
    write(text.plus("\n\n"))
}
fun PrintWriter.writeCode(text: String) {
    write(text.plus("\n"))
}

fun Float.format(digits: Int) = "%.${digits}f".format(this)