fun foo() {
    val execution = Execution().apply {
    id = executionService.saveExecution(this)}
    return execution.id!!
}

fun foo() {
    val execution = Execution().apply {
    id = executionService.saveExecution(this)
    sdk = this.defaultSdk(this)
    id2 = this.id + shift
    name = this.execution(this)}
    return execution.id!!
}
