fun String.createPluginConfig() {
    val pluginConfig = TomlDecoder.decode<T>(
        serializer(),
        fakeFileNode,
        DecoderConf()
    )
    pluginConfig.configLocation = this.toPath()
}

class HttpServer {
    val defaultPort = "8080"
    fun foo() {
        val httpClient = HttpClient("myConnection")
        httpClient.url = "http://example.com"
        httpClient.port = this.defaultPort
    }
}
