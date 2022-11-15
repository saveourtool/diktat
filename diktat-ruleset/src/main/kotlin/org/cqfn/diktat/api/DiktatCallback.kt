package org.cqfn.diktat.api

import java.util.function.BiConsumer

/**
 * Callback for diktat process
 */
@FunctionalInterface
fun interface DiktatCallback : BiConsumer<DiktatError, Boolean>
