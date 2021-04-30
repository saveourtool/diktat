package test.paragraph3.long_line

@Query(value = "select * from test inner join test_execution on test.id = test_execution.test_id and test_execution.st", nativeQuery = true)
fun retrieveBatches(limit: Int, offset: Int, executionId: Long): Some

@Query(value = "select * from test inner join test_execution on test.id = test_execution.test_id and test_execution.status = 'READY' and test_execution.test_suite_execution_id = ?3 limit ?1 offset ?2", nativeQuery = true)
fun some(limit: Int, offset: Int, executionId: Long): List<Test>

@Query(value = "select * from test inner joi", nativeQuery = true)
fun test(limit: Int, offset: Int, executionId: Long): List<Test>

@Query(value = "select * from test inner joibbb", nativeQuery = true)
fun cornerCase(limit: Int, offset: Int, executionId: Long): List<Test>

@Query(value = "select * from test inner join test_execution on test.id = test_execution.test_id and test_execution.status = 'READY' and test_execution.test_suite_execution_id = ?3 limit ?1 offset ?2", nativeQuery = true)
fun some(limit: Int, offset: Int, executionId: Long) = println("testtesttesttesttesttesttesttesttesttesttesttest")
