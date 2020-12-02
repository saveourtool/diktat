package test.paragraph3.block_brace

fun foo1() {
    if (x > 0) {
foo()
} else {
zoo()
}
}

fun foo1(){
    if (x < 0){
        println("Helloo")
    }
}

fun foo2(){
    if (x<0) {
println("Hello")
    }
}

fun foo3(){
    if (x == 5){
        println(5)
    } else if (x == 6)
        println(6)
    else
        println(7)
}

fun foo4(){
    if (x > 4){
        println(4)
    }else if (x < 4){
        println(5)
    }
}

fun foo() {
    //comment1
    // comment
if(x) {
        //comment2
        foo()
    }
}