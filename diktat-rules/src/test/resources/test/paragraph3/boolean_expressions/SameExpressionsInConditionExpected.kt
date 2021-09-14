fun foo() {
    if (a) {}
    if (a) {}
    if (a) {}

    return if (node is TomlKeyValueSimple) {
        decodeValue().toString().toLowerCase() != "null"
    } else {
        true
    }
}
