package test.chapter3.strings

val valueStr = "my str"
val x = 13

fun foo(): String {
    return "string"
}

val myTest1 = "my string " + "string " + valueStr + " other value"
val myTest2 = "my string " + 1 + valueStr + " other value"
val myTest3 = "my string " + valueStr + " string " + "other value"
val myTest4 = "my string" + (" one " + "two")
val myTest5 = "trying to sum with " + """multiline"""
val myTest6 = "trying to sum with " + """
    multiline""".trimIndent()
val myTest7 = """multiline""" + " string"
val myTest8 = "string " + valueStr.replace("my", "")
val myTest9 = "string " + "valueStr".replace("my", "")
val myTest10 = "string " + (1 + 5)
val myTest11 = "sum " + ("other string " + 3 + " str2 " + 5 + (" other string 2 " + 2 + " str3 " + 4))
val myTest12 = "my string " + 1 + 2 + 3
val myTest13 = (1 + 2).toString() + " my string " + 3 + " string " + valueStr + valueStr
val myTest14 = "my string " + (1 + 2 + 3) + (" other string " + 3) + (1 + 2 + 3)
val myTest15 = 1 + 2 + ("1" + 3).toInt()
val myTest16 = 1.0 + 2.0 + ("1" + 3.0).toFloat()
val myTest17 = "sum " + (1 + 2 + 3) * 4
val myTest18 = "my string " + (1 + 2 + 3) * 4 + (" other string " + 3) + (1 + (2 + 3)) + (" third string " + ("str " + 5))
val myTest19 = 1 + 2 + 3 + ("6" + 5).toInt()
val myTest20 = x.toString() + "string"
val myTest21 = x.toString() + " string"
val myTest22 = "string" + foo()
val myTest23 = x.toString() + foo()
val myTest24 = foo() + "string"
val myTest25 = "String " + valueStr?.value
val myTest26 = "my string " + if (true) "1" else "2"
