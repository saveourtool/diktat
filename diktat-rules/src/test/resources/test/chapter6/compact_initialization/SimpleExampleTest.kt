fun main() {
val httpClient = HttpClient("myConnection")
httpClient.url = "http://example.com"
httpClient.port = "8080"
httpClient.timeout = 100
httpClient.doRequest()
}