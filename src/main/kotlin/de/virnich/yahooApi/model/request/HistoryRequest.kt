package de.virnich.yahooApi.model.request

/**
 * Provides the path and query parameters necessary to query history data from Yahoo Finance API. Simplest way to
 * create a [HistoryRequest] is to use a [HistoryRequestBuilder].
 */
data class HistoryRequest(
    val symbol: String,
    val period: Period?,
    val interval: Interval,
    val start: Long,
    val end: Long
) {
    fun toParameterString(): String {

        val parameterMap = mutableMapOf<String, Any>()

        if (period == null) {
            parameterMap["period1"] = start
            parameterMap["period2"] = end
        } else {
            parameterMap["range"] = period.parameter
        }
        parameterMap["interval"] = interval.parameter

        return parameterMap.map { entry -> "${entry.key}=${entry.value}" }.joinToString("&")
    }
}
