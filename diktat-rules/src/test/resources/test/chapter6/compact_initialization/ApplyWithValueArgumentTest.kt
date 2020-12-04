fun main() {
val httpClient = HttpClient("myConnection").apply(::setDefaultUrl)
httpClient.port = "8080"
httpClient.timeout = 100
httpClient.doRequest()
}

fun setDefaultUrl(httpClient: HttpClient) {
    httpClient.url = "http://example.com"
}

fun foo() {
    val diktatExtension = project.extensions.create(DIKTAT_EXTENSION, DiktatExtension::class.java)
    diktatExtension.inputs = project.fileTree("src").apply {
        include("**/*.kt")
    }
    diktatExtension.reporter = PlainReporter(System.out)
}