package com.saveourtool.diktat

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.MultiGauge
import io.micrometer.core.instrument.Tags
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ActiveBinsMetric(meterRegistry: MeterRegistry, private val binRepository: BinRepository) {
    private val metric = MultiGauge.builder(ACTIVE_BINS_METRIC_NAME)
        .register(meterRegistry)

    @Scheduled(fixedDelay = DELAY)
    fun queryDb() {
        metric.register(
            binRepository
                .countActiveWithPartsNumber()
                .toRangeMap()
                .map {
                    MultiGauge.Row.of(
                        Tags.of(NUMBER_OF_EGGS_LABEL, it.key),
                        it.value
                    )
                }, true)
    }

    private fun List<NumberOfBinsAndParts>.toRangeMap(): MutableMap<String, Long> {
        var total = 0L
        val map = mutableMapOf<String, Long>()
        numberOfEggsBuckets.forEach { map[it] = 0 }

        this.forEach {
            total += it.numberOfBins
            when (it.numberOfParts) {
                1 -> map[EGG_1_BUCKET_LABEL] = it.numberOfBins
                2 -> map[EGG_2_BUCKET_LABEL] = it.numberOfBins
                3 -> map[EGG_3_BUCKET_LABEL] = it.numberOfBins
                in 4..5 -> map[EGG_4_5_BUCKET_LABEL] = it.numberOfBins
                in 7..9 -> map[EGG_7_9_BUCKET_LABEL] = it.numberOfBins
                in 10..Int.MAX_VALUE -> map[EGG_OVER_10_BUCKET_LABEL] = it.numberOfBins
            }
        }

        map[ALL_ACTIVE_BINS_LABEL] = total
        return map
    }

    companion object {
        private const val ACTIVE_BINS_METRIC_NAME = "c.concurrent.bins"
        private const val ALL_ACTIVE_BINS_LABEL = "total"
        private const val EGG_1_BUCKET_LABEL = "1"
        private const val EGG_2_BUCKET_LABEL = "2"
        private const val EGG_3_BUCKET_LABEL = "3"
        private const val EGG_4_5_BUCKET_LABEL = "4-5"
        private const val EGG_7_9_BUCKET_LABEL = "7-9"
        private const val EGG_OVER_10_BUCKET_LABEL = "10+"
        private const val NUMBER_OF_EGGS_LABEL = "numberOfEggs"
        private val numberOfEggsBuckets = setOf(
            EGG_1_BUCKET_LABEL,
            EGG_2_BUCKET_LABEL,
            EGG_3_BUCKET_LABEL,
            EGG_4_5_BUCKET_LABEL,
            EGG_7_9_BUCKET_LABEL,
            EGG_OVER_10_BUCKET_LABEL,
            ALL_ACTIVE_BINS_LABEL)
    }
}
