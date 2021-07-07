fun String.createPluginConfig() {
    val pluginConfig = TomlDecoder.decode<T>(
        serializer(),
        fakeFileNode,
        DecoderConf()
    ).apply {
    configLocation = this@createPluginConfig.toPath()}
}