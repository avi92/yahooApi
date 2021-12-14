package de.virnich.yahooApi.model.request

/**
 * Represents all available options for the Period query parameter.
 * A period will include the interval between the time of the query and the value of the interval.
 *
 * Example: [Period.ONE_DAY] will include all available data sets of the last 24 hours.
 */
enum class Period(val parameter: String) {
    ONE_DAY("1d"),
    FIVE_DAYS("5d"),
    ONE_MONTH("1m"),
    THREE_MONTHS("3m"),
    SIX_MONTHS("6m"),
    ONE_YEAR("1y"),
    TWO_YEARS("2y"),
    FIVE_YEARS("5y"),
    TEN_YEARS("10y"),
    MAX("max")
}
