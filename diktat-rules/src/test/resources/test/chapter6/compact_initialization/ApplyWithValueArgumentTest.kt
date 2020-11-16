fun main() {
val httpClient = HttpClient("myConnection").apply(::setDefaultUrl)
httpClient.port = "8080"
httpClient.timeout = 100
httpClient.doRequest()
}

fun setDefaultUrl(httpClient: HttpClient) {
    httpClient.url = "http://example.com"
}