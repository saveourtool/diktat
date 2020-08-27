package test.paragraph3.spaces

data class User(val name: String, val id: Int)

fun main() {
    val user = User("Alex", 1)
    println(user)

    val secondUser = User("Alex", 1)
    val thirdUser = User("Max", 2)

    println("user == secondUser: ${user == secondUser}")
    println("user == thirdUser: ${user == thirdUser}")

    println(user.hashCode())
    println(thirdUser.hashCode())

    println(user.copy())
    println(user.copy("Max"))
    println(user.copy(id = 2))

    println("name = ${user.component1()}")
    println("id = ${user.component2()}")
}
