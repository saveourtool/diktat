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

    // long case
    if (a > 5 && ((b > 6 && z > 3) || (c > 7 && y > 4) || (d > 8 && w > 5))) {

    }

    // long case #2.1
    if (b > 6 && a > 5 && (z > 3 || c > 7 || w > 5)) {

    }

    // long case #2.2
    if (b > 6 || a > 5 || (z > 3 && c > 7 && w > 5)) {

    }
}
