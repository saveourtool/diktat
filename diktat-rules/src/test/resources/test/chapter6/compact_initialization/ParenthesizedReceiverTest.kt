fun `translate text`() {
    val res = translateText(text = "dummy")
    (res is TranslationsSuccess) shouldBe true
    val translationsSuccess = res as TranslationsSuccess
    translationsSuccess.translations = 1
}
