fun foo() {
    val execution = Execution()
    execution.id = executionService.saveExecution(execution)
    return execution.id!!
}

fun foo() {
    val execution = Execution()
    execution.id = executionService.saveExecution(execution)
    execution.sdk = execution.defaultSdk(execution)
    execution.id2 = execution.id + shift
    execution.name = execution.execution(execution)
    return execution.id!!
}

fun foo(line: String) {
    val pair = line.split("=", limit = 2).map {
        it.replace("\\=", "=")
    }
    pair.first() to pair.last()
}

