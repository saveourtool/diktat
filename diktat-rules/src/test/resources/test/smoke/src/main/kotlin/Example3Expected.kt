package org.cqfn.diktat

/*
 * Copyright (c) Your Company Name Here. 2010-2020
 */

/**
 * @property name
 */
class HttpClient(var name: String) {
    var url: String = ""
    var port: String = ""
    var timeout = 0

    fun doRequest() {}
}

class Example {
    fun foo() {
        if (condition1 && condition2) {
            bar()
        }

        if (condition3) {
            if (condition4) {
                foo()
            } else {
                bar()
            }
        } else {
            foo()
        }
    }
}

enum class IssueType {
    PROJECT_STRUCTURE, TESTS, VCS
}

enum class IssueType2 {
    PROJECT_STRUCTURE,
    TESTS,
    VCS,
    ;

    /**
     * @param bar
     * @return
     */
    fun foo(bar: Int) = bar

    companion object
}

class Foo {
    /**
     * @implNote lorem ipsum
     */
    private fun foo() {}
}

fun mains() {
    val httpClient = HttpClient("myConnection")
        .apply {
            url = "http://example.com"
            port = "8080"
            timeout = 100
        }
    httpClient.doRequest()
}
