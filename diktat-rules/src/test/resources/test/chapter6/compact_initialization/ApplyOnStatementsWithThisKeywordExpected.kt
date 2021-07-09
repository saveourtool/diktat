fun String.createPluginConfig() {
    val pluginConfig = TomlDecoder.decode<T>(
        serializer(),
        fakeFileNode,
        DecoderConf()
    ).apply {
    prop1 = property1
    // comment2
    prop2 = property2}
    pluginConfig.configLocation = this.toPath()
    // comment1
    pluginConfig.configLocation2 = this.toPath()
}

