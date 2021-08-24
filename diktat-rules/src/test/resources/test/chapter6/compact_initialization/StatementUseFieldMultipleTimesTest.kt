private fun foo() {
    val execution = Execution()
    execution.id = executionService.saveExecution(execution)
    return execution.id!!
}