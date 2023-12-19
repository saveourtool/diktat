package whate.ver

fun String.createPluginConfig() {
    val pluginConfig = TomlDecoder.decode<T>(
        serializer(),
        fakeFileNode,
        DecoderConf()
    )
    pluginConfig.configLocation = this.toPath()
    pluginConfig.prop1 = property1
    // comment1
    pluginConfig.configLocation2 = this.toPath()
    // comment2
    pluginConfig.prop2 = property2
}
