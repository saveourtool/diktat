package test.paragraph2.kdoc

/**
 * This is the short overview comment for the example interface.
 *                   /* Add a blank line between the general comment text and each KDoc tag */
 * @since 1.6
 */
public interface Example {
    // Some comments  /* Since it is the first member definition in this code block, there is no need to add a blank line here */
    val aField: String

// Some comments
    val bField: String

/**
     * This is a long comment with whitespace that should be split in
     * multiple line comments in case the line comment formatting is enabled.
     *                /* blank line between description and Kdoc tag */
     * @return the rounds of battle of fox and dog
     */
    fun foo()

    /**
     * These possibilities include: Formatting of header comments
     * @return the rounds of battle of fox and dog
     */
    fun bar() {
        // Some comments  /* Since it is the first member definition in this range, there is no need to add a blank line here */
        var aVar = 5

        // Some comments  /* Add a blank line above the comment */
        fun doSome() {

        }
    }
}
