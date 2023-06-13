package test.paragraph3.file_structure

import com.android.*
import kotlin.system.exitProcess
// comment about java imports
import java.io.IOException
import java.net.URL
import com.saveourtool.*
import com.fasterxml.jackson.databind.ObjectMapper
import android.*
import com.saveourtool.diktat.*
import org.junit.jupiter.api.Assertions
import androidx.*
import org.springframework.context.annotation.Bean
import com.google.common.base.CaseFormat
import java.nio.charset.Charset
import io.gitlab.arturbosch.detekt.Detekt
import org.slf4j.Logger

class Example {
    val x = setOf<Object>(CaseFormat(), Detekt(), Assertions(), Logger(), Bean(), IOException(), URL(), Charset(), ObjectMapper())

    fun Foo() {
        while (true) {
            exitProcess(1)
        }
    }
}
