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
        result?.let {
foo()
} ?: break
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
        result?.let {
foo()
} ?: break
    }
}

fun foo() {
    var result: Int? = 19
    while(result != 0 ) {
        result ?: break
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
            break
        } else {
            break
        }
    }
}

fun foo() {
    var result: Int? = 19
    while(result != 0 ) {
        if (result != null) {
            result?.let {
goo()
} ?: break
        } else {
            break
        }
    }
}

