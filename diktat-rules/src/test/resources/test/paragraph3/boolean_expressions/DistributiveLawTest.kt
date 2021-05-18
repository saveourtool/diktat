package test.paragraph3.boolean_expressions

fun some() {
    if (a > 5 && b > 6 || c > 7 && a > 5) {

    }

    if ((a > 5 || b > 6) && (c > 7 || a > 5)) {

    }

    if ((a > 5 || b > 6) && (c > 7 || a > 5) && (a > 5 || d > 8)) {

    }

    if ((a > 5 && b > 6) || (c > 7 && a > 5) || (a > 5 && d > 8)) {

    }

    // Special case
    if ((b > 6 && a > 5) || (c > 7 && a > 5) || (a > 5 && d > 8)) {

    }
}
