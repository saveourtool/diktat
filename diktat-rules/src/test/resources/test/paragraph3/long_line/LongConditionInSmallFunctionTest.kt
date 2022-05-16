package test.paragraph3.long_line

private fun isContainingRequiredPartOfCode(text: String): Boolean =
            text.contains("val ", true) || text.contains("var ", true) || text.contains("=", true) || (text.contains("{", true) && text.substringAfter("{").contains("}", true))
