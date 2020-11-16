fun main() {
val httpClient = HttpClient("myConnection").apply {
url = "http://example.com"
port = "8080"
timeout = 100}
httpClient.doRequest()
}