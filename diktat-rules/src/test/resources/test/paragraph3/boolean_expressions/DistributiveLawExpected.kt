package test.paragraph3.boolean_expressions

fun some() {
    if (a > 5 && (b > 6 || c > 7)) {

    }

    if (a > 5 || (b > 6 && c > 7)) {

    }

    if (a > 5 || (b > 6 && c > 7 && d > 8)) {

    }

    if (a > 5 && (b > 6 || c > 7 || d > 8)) {

    }

    // Special case
    if (a > 5 && (b > 6 || c > 7 || d > 8)) {

    }
}
