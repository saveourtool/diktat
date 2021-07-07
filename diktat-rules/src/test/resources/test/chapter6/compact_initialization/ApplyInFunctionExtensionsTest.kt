fun String.createPluginConfig() {
    val pluginConfig = TomlDecoder.decode<T>(
        serializer(),
        fakeFileNode,
        DecoderConf()
    )
    pluginConfig.configLocation = this.toPath()
}