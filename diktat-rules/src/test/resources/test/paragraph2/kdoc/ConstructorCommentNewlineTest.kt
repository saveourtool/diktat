package test.paragraph2.kdoc

class Example(
    val param1: String, // first comment
    val param2: String, // second comment
)

class Example(
    val param1: String, /* first comment */
    val param2: String, /* second comment */
)

class Example(
    val param1: String, /** first comment */
    val param2: String, /** second comment */
)
