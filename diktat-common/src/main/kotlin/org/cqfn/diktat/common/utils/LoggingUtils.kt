/**
 * Utilities related to logging
 */

package org.cqfn.diktat.common.utils

import com.pinterest.ktlint.core.initKtLintKLogger
import mu.KotlinLogging
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

/**
 * Create a logger using [KotlinLogging] and configure it by ktlint's mechanism
 *
 * @param func empty fun which is used to get enclosing class name
 * @return a logger
 */
fun KotlinLogging.loggerWithKtlintConfig(func: () -> Unit) =
    logger(func).initKtLintKLogger()

/**
 * Create a logger using [KotlinLogging] and configure it by ktlint's mechanism
 *
 * @param clazz a class for which logger is needed
 * @return a logger
 */
fun KotlinLogging.loggerWithKtlintConfig(clazz: KClass<*>) =
    logger(LoggerFactory.getLogger(clazz.java)).initKtLintKLogger()
