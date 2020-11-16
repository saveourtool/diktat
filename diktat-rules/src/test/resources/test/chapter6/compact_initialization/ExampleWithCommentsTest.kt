fun main() {
val httpClient = HttpClient("myConnection")
// assigning URL
httpClient.url = "http://example.com"

// setting port to 8080
httpClient.port = "8080"
/* we set timeout
in case it times out
*/
httpClient.timeout = 100
// finally, we can make request
httpClient.doRequest()
}