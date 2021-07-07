fun String.createPluginConfig() {
    val pluginConfig = TomlDecoder.decode<T>(
        serializer(),
        fakeFileNode,
        DecoderConf()
    ).apply {
    configLocation = this@createPluginConfig.toPath()}
}

class HttpServer {
    val defaultPort = "8080"
    fun foo() {
        val httpClient = HttpClient("myConnection").apply {
        url = "http://example.com"
        port = this@HttpServer.defaultPort}
    }
}
