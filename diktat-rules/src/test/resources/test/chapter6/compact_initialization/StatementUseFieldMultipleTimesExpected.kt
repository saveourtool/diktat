private fun foo() {
    val execution = Execution().apply {
    id = executionService.saveExecution(this)}
    return execution.id!!
}