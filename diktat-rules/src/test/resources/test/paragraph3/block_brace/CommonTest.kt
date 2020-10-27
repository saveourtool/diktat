package org.cqfn.diktat.resources.test.paragraph3.block_brace

class HttpClient(var name: String) {
    var url: String = ""
    var port: String = ""
    var timeout = 0

    fun doRequest() {}
}

fun mains() {
    val httpClient = HttpClient("myConnection")
        .apply {
            url = "http://pushkin.com"
            port = "8080"
            timeout = 100
        }
        .doRequest()
}
