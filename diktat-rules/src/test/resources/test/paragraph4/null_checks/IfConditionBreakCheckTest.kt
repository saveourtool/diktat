package test.paragraph4.null_checks


fun foo() {
    var result: Int? = 19
    while(result != 0 ) {
        if (result == null) {
            foo()
        } else {
            break
        }
    }
}

fun foo() {
    var result: Int? = 19
    while(result != 0 ) {
        if (result == null) {
            break
        } else {
            foo()
        }
    }
}

fun foo() {
    var result: Int? = 19
    while(result != 0 ) {
        if (result != null) {
            break
        } else {
            foo()
        }
    }
}

fun foo() {
    var result: Int? = 19
    while(result != 0 ) {
        if (result != null) {
            foo()
        } else {
            break
        }
    }
}

////////////////////////////////

fun foo() {
    var result: Int? = 19
    while(result != 0 ) {
        if (result == null) {
            foo()
        }
    }
}

fun foo() {
    var result: Int? = 19
    while(result != 0 ) {
        if (result == null) {
            break
        }
    }
}

fun foo() {
    var result: Int? = 19
    while(result != 0 ) {
        if (result != null) {
            break
        }
    }
}

fun foo() {
    var result: Int? = 19
    while(result != 0 ) {
        if (result != null) {
            foo()
        }
    }
}

