fun main() {
val httpClient = HttpClient("myConnection").apply {
// assigning URL
url = "http://example.com"

// setting port to 8080
port = "8080"
/* we set timeout
in case it times out
*/
timeout = 100}
// finally, we can make request
httpClient.doRequest()
}