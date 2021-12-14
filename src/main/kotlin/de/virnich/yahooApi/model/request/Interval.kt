package de.virnich.yahooApi.model.request

/**
 * Represents all available options for the interval query parameter.
 * An interval represents the period of time between queried history data points.
 */
enum class Interval(val parameter: String) {
    ONE_MINUTE("1m"),
    TWO_MINUTES("2m"),
    FIVE_MINUTES("5m"),
    FIFTEEN_MINUTES("15m"),
    THIRTY_MINUTES("30m"),
    SIXTY_MINUTES("60m"),
    NINETY_MINUTES("90m"),
    ONE_HOUR("1h"),
    ONE_DAY("1d"),
    FIVE_DAYS("5d"),
    ONE_WEEK("1wk"),
    ONE_MONTH("1mo"),
    THREE_MONTHS("3mo")
}
