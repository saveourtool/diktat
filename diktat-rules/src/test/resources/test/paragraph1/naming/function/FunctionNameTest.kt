package org.cqfn.diktat.ktlint.ruleset

class TestPackageName {
    fun /* */ METHOD1(someName: String): Unit {

    }

    fun /* */ method_two(someName: String): Unit {
        fun other_method_inside(): Boolean {
            return false
        }
    }

    fun /* */ methODTREE(someName: String) {

    }
}

// incorrect case
fun /* */ String.STRMETHOD1(someName: String): Unit {

}

// incorrect case
fun /* */ String.str_method_two(someName: String): Unit {

}

// incorrect case
fun /* */ String.strMethODTREE(): String {
    return ""
}

// should be corrected to isValidIdentifier
fun /* */ String.validIdentifier(): Boolean {
    return false
}
