package test.paragraph2.kdoc

class Example {
    /**
 * @return
 */
fun hasNoChildren() = children.size == 0

    /**
 * @return
 */
fun getFirstChild() = children.elementAtOrNull(0)

    @GetMapping("/projects")
    fun getProjects() = projectService.getProjects()
}