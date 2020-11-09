package test.smoke.src.main.kotlin

class HttpClient {
    var name: String
    var url: String = ""
    var port: String = ""
    var timeout = 0

    constructor(name: String) {
        this.name = name
    }

    fun doRequest() {}
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