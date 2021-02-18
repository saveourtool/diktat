package test.paragraph2.kdoc

/**
 * Creates a docker container with [file], prepared to execute it
 *
 * @param runConfiguration a [RunConfiguration] for the supplied binary
 * @param file a file that will be included as an executable
 * @param resources additional resources
 * @throws DockerException if docker daemon has returned an error
 * @throws DockerException if docker daemon has returned an error
 * @throws RuntimeException if an exception not specific to docker has occurred
 * @return id of created container or null if it wasn't created
 */
internal fun createWithFile(runConfiguration: RunConfiguration,
                            containerName: String,
                            file: File,
                            resources: Collection<File> = emptySet()): String {}