package test.paragraph2.kdoc

class Example {
    fun hasNoChildren() = children.size == 0

    fun getFirstChild() = children.elementAtOrNull(0)

    @GetMapping("/projects")
    fun getProjects() = projectService.getProjects()
}