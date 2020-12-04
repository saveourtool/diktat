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

fun foo() {
    val diktatExtension = project.extensions.create(DIKTAT_EXTENSION, DiktatExtension::class.java).apply {
    inputs = project.fileTree("src").apply {
        include("**/*.kt")
    }
    reporter = PlainReporter(System.out)}
}