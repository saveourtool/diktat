package com.saveourtool.diktat.ruleset.chapter3.spaces

import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig

internal object IndentationConfigFactory {
    /**
     * Creates an `IndentationConfig` from zero or more
     * [config entries][configEntries]. Invoke without arguments to create a
     * default `IndentationConfig`.
     *
     * @param configEntries the configuration entries to create this instance from.
     * @return the configuration created.
     * @see [IndentationConfig]
     */
    operator fun invoke(vararg configEntries: Pair<String, Any>): IndentationConfig =
        IndentationConfig(mapOf(*configEntries).mapValues { (_, value) ->
            value.toString()
        })
}
