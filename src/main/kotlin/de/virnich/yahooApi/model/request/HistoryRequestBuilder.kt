package de.virnich.yahooApi.model.request

import org.apache.commons.lang3.StringUtils
import java.time.*

/**
 * Helper class to create a [HistoryRequest] following the Builder Pattern.
 *
 * By default, all available daily chart data will be queried.
 * Keep in mind that the quality and wholeness of the data set may vary.
 *
 * Minimum Example: HistoryRequestBuilder("AAPL").build()
 */
class HistoryRequestBuilder(private val symbol: String) {

    private var period: Period? = null
    private var interval: Interval = Interval.ONE_DAY
    private var start: LocalDate = LocalDate.of(1970, 1, 1)
    private var end: LocalDate? = null
    private var zoneId: ZoneId = ZoneId.systemDefault()

    /**
     * Determines the [Interval] between the history data.
     *
     * @return [HistoryRequestBuilder]
     */
    fun withInterval(interval: Interval): HistoryRequestBuilder {
        this.interval = interval
        return this
    }

    /**
     * Determines the start time, from when the data should be retrieved.
     *
     * @return [HistoryRequestBuilder]
     */
    fun withStart(start: LocalDate): HistoryRequestBuilder {
        this.start = start
        return this
    }

    /**
     * Determines the last time, from when the data should be retrieved.
     *
     * @return [HistoryRequestBuilder]
     */
    fun withEnd(end: LocalDate): HistoryRequestBuilder {
        this.end = end
        return this
    }

    /**
     * Determines the [Period] for which the data should be retrieved.
     * If a period is set, the [HistoryRequest.start] and [HistoryRequest.end] parameters will be ignored.
     *
     * @return [HistoryRequestBuilder]
     */
    fun withPeriod(period: Period): HistoryRequestBuilder {
        this.period = period
        return this
    }

    /**
     * The zoneId is by default set to [ZoneId.systemDefault]. You may optionally change this to a different value,
     * which will impact the conversion of [start] and [end] to [ZonedDateTime.toEpochSecond].
     *
     * @return [HistoryRequestBuilder]
     */
    fun withZoneId(zoneId: ZoneId): HistoryRequestBuilder {
        this.zoneId = zoneId
        return this
    }

    /**
     * Returns a [HistoryRequest] based on the parameter selection of the [HistoryRequestBuilder]
     *
     * @return [HistoryRequest]
     */
    fun build(): HistoryRequest {

        if (!StringUtils.isNotBlank(this.symbol)) {
            throw RuntimeException("No symbol provided!")
        }
        if (end == null) {
            end = LocalDate.now()
        }
        val startMillis = ZonedDateTime.of(LocalDateTime.of(this.start, LocalTime.MIN), this.zoneId).toEpochSecond()
        val endMillis = ZonedDateTime.of(LocalDateTime.of(this.end, LocalTime.MIN), this.zoneId).toEpochSecond()

        return HistoryRequest(symbol, period, interval, startMillis, endMillis)
    }

}
