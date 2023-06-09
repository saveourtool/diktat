package com.saveourtool.diktat.test.paragraph1.naming.generic

private class ClassName<T> {
    private fun <Template, T> lock(body: ((Template?) -> T?)?, value: Template?): T? {
        try {
            val variableName: Template? = null
            val variableT: T? = null
            println(variableT)
            return body!!(variableName)
        } finally {
            println()
        }
    }

    fun foo(var1: T, var2: ((T?) -> T?)?) {
        lock<T, T>(var2, var1)
    }
}
