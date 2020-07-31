package test.paragraph3.statement

import com.pinterest.ktlint.core.KtLint
 import com.pinterest.ktlint.core.LintError

fun foo(){
    if (x > 0){
        goo()
 qwe()
    }
}

fun foo(){
    if (x > 0){
        goo()
qwe()
    }
}

fun foo() {
     grr()
}

enum class ProtocolState {
    WAITING {
        override fun signal() = TALKING
    },
    TALKING {
        override fun signal() = WAITING
    };
 abstract fun signal(): ProtocolState
}