package de.virnich.yahooApi.model

import java.math.BigDecimal

data class Ohlcv(
    val open: BigDecimal?,
    val high: BigDecimal?,
    val low: BigDecimal?,
    val volume: Long?,
    val close: BigDecimal?
) {
    constructor(open: BigDecimal?, high: BigDecimal?, low: BigDecimal?, volume: BigDecimal?, close: BigDecimal?) : this(
        open,
        high,
        low,
        volume?.longValueExact(),
        close
    )
}
