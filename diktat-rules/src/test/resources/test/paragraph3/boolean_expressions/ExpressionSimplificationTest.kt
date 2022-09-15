package test.paragraph3.boolean_expressions

fun F.foo1() {
    if (!(this.valueParameters[i].getFunctionName() == other.valueParameters[i].getFunctionName() &&
                this.valueParameters[i].getFunctionType() != other.valueParameters[i].getFunctionType())
    ) {
        return false
    }
}

fun F.foo2() {
    if (!(this.valueParameters[i].getFunctionName() > other.valueParameters[i].getFunctionName() &&
                this.valueParameters[i].getFunctionType() < other.valueParameters[i].getFunctionType())
    ) {
        return false
    }
}

fun F.foo3() {
    if (!(this.valueParameters[i].getFunctionName() xor other.valueParameters[i].getFunctionName() &&
                this.valueParameters[i].getFunctionType() xor other.valueParameters[i].getFunctionType())
    ) {
        return false
    }
}
