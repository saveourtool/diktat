package test.chapter6.classes

class Test {
var a: Int

constructor(a: Int) {
    this.a = a
}
}

class Test {
var a: Int

constructor(_a: Int) {
    a = _a
}
}

class Test {
var a: Int

constructor(_a: Int) {
    var a = 14
    a = _a
    this.a = _a
}
}