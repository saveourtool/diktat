package org.cqfn.diktat.scripts

import kotlinx.browser.document

fun main() {
    (document.getElementById("myId") as HTMLElement).click()
}
