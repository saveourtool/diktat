package org.cqfn.diktat.ruleset.utils

import java.io.PrintWriter

val NUMBER_IN_TAG = Regex("\"([a-z0-9.]*)\"") // finds "r1.0.2"
val RULE_NAME = Regex("(</a>[A-Za-z 0-9.-]*)") // get's rule name from ### <a>...</a> Rule name
val BOLD_TEXT = Regex("""\*\*([^*]+)\*\*""") // finds bold text in regular lines
val ITALIC_TEXT = Regex("""\*([A-Za-z ]+)\*""") // finds italic text in regular lines
val BACKTICKS_TEXT = Regex("""`([^`]*)`""") // finds backtick in regular text (not used for now, may be we will need to use it in future)
val ANCHORS = Regex("""\(#(.*)\)""") // finds anchors on rules and deletes them
val TABLE_COLUMN_NAMES = Regex("""[A-Za-z ]*""")  // used to find column names in tables only
const val REGEX_PLACEHOLDER = "RE_PL_AC_E_ME"
const val A4_PAPER_WIDTH = 15f

fun PrintWriter.writeln(text: String) {
    write(text.plus("\n\n"))
}
fun PrintWriter.writeCode(text: String) {
    write(text.plus("\n"))
}

fun Float.format(digits: Int) = "%.${digits}f".format(this)

