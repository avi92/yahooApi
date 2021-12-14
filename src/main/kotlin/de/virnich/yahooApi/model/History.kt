package de.virnich.yahooApi.model

import com.yahoo.finance.query2.models.Meta
import java.time.LocalDateTime

data class History(
    val chart: Map<LocalDateTime, Ohlcv>,
    val meta: Meta
)
