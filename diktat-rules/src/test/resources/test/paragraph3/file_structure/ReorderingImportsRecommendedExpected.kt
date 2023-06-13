package test.paragraph3.file_structure

// comment about java imports
import android.*
import androidx.*
import com.android.*

import com.saveourtool.*
import com.saveourtool.diktat.*

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.CaseFormat
import io.gitlab.arturbosch.detekt.Detekt
import org.junit.jupiter.api.Assertions
import org.slf4j.Logger
import org.springframework.context.annotation.Bean

import java.io.IOException
import java.net.URL
import java.nio.charset.Charset

import kotlin.system.exitProcess

class Example {
    val x = setOf<Object>(CaseFormat(), Detekt(), Assertions(), Logger(), Bean(), IOException(), URL(), Charset(), ObjectMapper())

    fun Foo() {
        while (true) {
            exitProcess(1)
        }
    }
}
