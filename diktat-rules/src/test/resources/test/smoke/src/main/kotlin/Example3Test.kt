/*
 * Copyright (c) Your Company Name Here. 2010-2020
 */

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
                url = "http://pushkin.com"
                port = "8080"
                timeout = 100
            }
    httpClient.doRequest()
}