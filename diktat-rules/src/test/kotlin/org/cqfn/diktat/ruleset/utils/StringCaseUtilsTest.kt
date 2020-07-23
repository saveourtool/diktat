package org.cqfn.diktat.ruleset.utils

import org.junit.Assert
import org.junit.Test

class StringCaseUtilsTest {
    @Test
    fun `check conversion to upperSnakeCase`() {
        Assert.assertEquals("PASCAL_CASE", "PascalCase".toUpperSnakeCase())
        Assert.assertEquals("LOWER_SNAKE", "lower_snake".toUpperSnakeCase())
        Assert.assertEquals("I_AM_CONSTANT", "iAmConstant".toUpperSnakeCase())
        Assert.assertEquals("PASCAL_N_CASE", "PascalN_Case".toUpperSnakeCase())
    }

    @Test
    fun `check conversion to lowerCamelCase`() {
        Assert.assertEquals("strangeName", "STRANGE_name".toLowerCamelCase())
        Assert.assertEquals("strangeName", "STRANGE_NAME".toLowerCamelCase())
        Assert.assertEquals("sTrangeName", "sTrange_NAME".toLowerCamelCase())
        Assert.assertEquals("sTrangeName", "sTRange_NAME".toLowerCamelCase())
        Assert.assertEquals("sTrangeName", "__sTRange_NAME".toLowerCamelCase())
        Assert.assertEquals("pascalNCase", "PascalN_Case".toLowerCamelCase())
    }

    @Test
    fun `check conversion to PascalCase`() {
        Assert.assertEquals("StrangeName", "STRANGE_name".toPascalCase())
        Assert.assertEquals("StrangeName", "STRANGE_NAME".toPascalCase())
        Assert.assertEquals("StrangeName", "sTrange_NAME".toPascalCase())
        Assert.assertEquals("KdocTest", "KDoc_test".toPascalCase())
        Assert.assertEquals("StrangeName", "sTRange_NAME".toPascalCase())
        Assert.assertEquals("StrangeName", "__sTRange_NAME".toPascalCase())
        Assert.assertEquals("PascalNCase", "PascalN_Case".toPascalCase())
    }
}
