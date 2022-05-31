package test.paragraph3.boolean_expressions

fun foo() {
    if (bar && (!isEmpty() || isEmpty())) {
    }
    if (bar && (isEmpty() || !isEmpty())) {
    }
}
