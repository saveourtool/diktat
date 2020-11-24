fun main() {
val httpClient = HttpClient("myConnection").apply {
    setDefaultUrl(this)
port = "8080"
timeout = 100
}
httpClient.doRequest()
}

fun setDefaultUrl(httpClient: HttpClient) {
    httpClient.url = "http://example.com"
}