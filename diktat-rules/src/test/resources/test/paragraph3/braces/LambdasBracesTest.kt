package test.paragraph3.braces

val lambda1: (String) -> Unit = { name: String -> {println("Hello, World!")} }
val lambda2: (String) -> Unit = { name ->{ println("Hello, World!") }}
val lambda4: (String) -> Unit = { println("Hello, World!") }
val lambda5: (Int, Int) -> Int = { x, y ->
    print(x)
    print(y)
    x + y
}
val lambda6 = {x: Int, y: Int -> {x + y} }