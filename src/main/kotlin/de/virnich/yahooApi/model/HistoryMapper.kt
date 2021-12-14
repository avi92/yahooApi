package de.virnich.yahooApi.model

import com.yahoo.finance.query2.models.Result
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class HistoryMapper {

    fun mapYahooChartToHistory(result: Result): History {

        val chart = mutableMapOf<LocalDateTime, Ohlcv>()
        val quote = result.indicators!!.quote!![0]

        var localDateTime: LocalDateTime
        var ohlcv: Ohlcv
        for (i in 0 until result.timestamp!!.size) {
            try {
                localDateTime =
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(result.timestamp[i] * 1000), ZoneId.systemDefault())
                ohlcv = Ohlcv(
                    quote.open?.get(i),
                    quote.high?.get(i),
                    quote.low?.get(i),
                    quote.volume?.get(i),
                    quote.close?.get(i)
                )
            } catch (ex: Exception) {
                throw RuntimeException("Error mapping history data!", ex)
            }
            if ((ohlcv.open != null || ohlcv.close != null || ohlcv.high != null || ohlcv.low != null || ohlcv.volume != null)) {
                chart[localDateTime] = ohlcv
            }
        }

        return History(chart.toMap(), result.meta!!)
    }
}
