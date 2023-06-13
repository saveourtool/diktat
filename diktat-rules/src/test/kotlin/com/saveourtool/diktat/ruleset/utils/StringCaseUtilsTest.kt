package com.saveourtool.diktat.ruleset.utils

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class StringCaseUtilsTest {
    @Test
    fun `check conversion to upperSnakeCase`() {
        Assertions.assertEquals("PASCAL_CASE", "PascalCase".toUpperSnakeCase())
        Assertions.assertEquals("LOWER_SNAKE", "lower_snake".toUpperSnakeCase())
        Assertions.assertEquals("I_AM_CONSTANT", "iAmConstant".toUpperSnakeCase())
        Assertions.assertEquals("PASCAL_N_CASE", "PascalN_Case".toUpperSnakeCase())
    }

    @Test
    fun `check conversion to lowerCamelCase`() {
        Assertions.assertEquals("strangeName", "STRANGE_name".toLowerCamelCase())
        Assertions.assertEquals("strangeName", "STRANGE_NAME".toLowerCamelCase())
        Assertions.assertEquals("sTrangeName", "sTrange_NAME".toLowerCamelCase())
        Assertions.assertEquals("sTrangeName", "sTRange_NAME".toLowerCamelCase())
        Assertions.assertEquals("sTrangeName", "__sTRange_NAME".toLowerCamelCase())
        Assertions.assertEquals("pascalNCase", "PascalN_Case".toLowerCamelCase())
    }

    @Test
    fun `check conversion to PascalCase`() {
        Assertions.assertEquals("StrangeName", "STRANGE_name".toPascalCase())
        Assertions.assertEquals("StrangeName", "STRANGE_NAME".toPascalCase())
        Assertions.assertEquals("StrangeName", "sTrange_NAME".toPascalCase())
        Assertions.assertEquals("KdocTest", "KDoc_test".toPascalCase())
        Assertions.assertEquals("StrangeName", "sTRange_NAME".toPascalCase())
        Assertions.assertEquals("StrangeName", "__sTRange_NAME".toPascalCase())
        Assertions.assertEquals("PascalNCase", "PascalN_Case".toPascalCase())
    }
}
