fun foo() {
    if (a || a) {}
    if (a && a) {}
    if ((((a && a)))) {}

    return if ((node is TomlKeyValueSimple) || (node is TomlKeyValueSimple)) {
        decodeValue().toString().toLowerCase() != "null"
    } else {
        true
    }
}
