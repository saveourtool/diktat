package test.paragraph2.kdoc

/**
 * @property param1
 * @property param2 first comment
 */
class Example(
    val param1: String,
    val param2: String, // second comment
)

/**
 * @property param1
 * @property param2 first comment
 */
class Example(
    val param1: String,
    val param2: String, /* second comment */
)

/**
 * @property param1
 * @property param2 first comment
 */
class Example(
    val param1: String,
    val param2: String, /** second comment */
)
