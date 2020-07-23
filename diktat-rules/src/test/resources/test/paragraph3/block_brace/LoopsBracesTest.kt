package test.paragraph3.block_brace

fun foo(){
    for (i in 1..100){
        println(i)
    }
    for (i in 1..100) {
        println(i)}

    for (i in 1..100) { println(i) }
}