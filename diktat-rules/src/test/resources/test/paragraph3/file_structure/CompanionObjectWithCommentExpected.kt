package com.saveourtool.diktat

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

/**
 * @param meterRegistry
 * @param binRepository
 */
@Component
class ActiveBinsMetric(meterRegistry: MeterRegistry, private val binRepository: BinRepository) {
    companion object {
        private const val EGG_4_5_BUCKET_LABEL = "4-5"
        private const val EGG_3_BUCKET_LABEL = "3"
        private const val EGG_OVER_10_BUCKET_LABEL = "10+"
        private const val EGG_7_9_BUCKET_LABEL = "7-9"
        private const val DELAY = 15000L

        // 15s
        private const val ACTIVE_BINS_METRIC_NAME = "c.concurrent.bins"
        private const val NUMBER_OF_EGGS_LABEL = "numberOfEggs"
        private const val ALL_ACTIVE_BINS_LABEL = "total"
        private const val EGG_2_BUCKET_LABEL = "2"
        private const val EGG_1_BUCKET_LABEL = "1"
        private val numberOfEggsBuckets = setOf(
            EGG_4_5_BUCKET_LABEL,
            EGG_2_BUCKET_LABEL,
            EGG_3_BUCKET_LABEL,
            EGG_7_9_BUCKET_LABEL,
            EGG_1_BUCKET_LABEL,
            EGG_OVER_10_BUCKET_LABEL,
            ALL_ACTIVE_BINS_LABEL)
    }
}
