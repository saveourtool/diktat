package test.chapter3.strings

val valueStr = "my str"

val myTest1 = "my string " + "string " + valueStr + " other value"
val myTest2 = "my string " + 1 + valueStr + " other value"
val myTest3 = "my string " + valueStr + " string " + " other value"
val myTest4 = "my string" + (" one " + "two")
val myTest5 = "trying to sum with " + """multiline"""
val myTest6 = "trying to sum with " + """
    multiline""".trimIndent()
val myTest7 = """multiline""" + " string"
val myTest8 = "string " + valueStr.replace("my", "")
val myTest9 = "string " + "valueStr".replace("my", "")
val myTest10 = "string " + (1 + 5)

val myTest12 = "my string " + 1 + 2 + 3
val myTest13 = (1 + 2).toString() + " my string " + 3 + " string " + valueStr + valueStr
val myTest14 = "my string " + (1 + 2 + 3) + ("other string " + 3) + (1 + 2 + 3)
val myTest15 = 1 + 2 + ("1" + 3).toInt()
val myTest16 = 1.0 + 2.0 + ("1" + 3.0).toFloat()
val myTest17 = 1.0 + 2.0 + ("1" + 3.0).toFloat()
val myTest18 = "my string " + (1 + 2 + 3) * 4 + (" other string " + 3) + (1 + (2 + 3)) + (" third string " + (" str " + 5))
val myTest19 = 1 + 2 + 3 + ("6" + 5).toInt()
