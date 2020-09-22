package test.smoke

class example{ @get : JvmName ( "getIsValid" )
    val isValid = true

    val foo:Int =1
fun bar(x :Int,y:Int) :Int {
return   x+ y}

    fun String.countSubStringOccurrences(sub: String): Int {
        // println("sub: $sub")
        return this.split(sub).size - 1
    }

    fun String.splitPathToDirs(): List<String> =
            this.replace("\\", "/")        .replace("//", "/")
                    .split("/")
}