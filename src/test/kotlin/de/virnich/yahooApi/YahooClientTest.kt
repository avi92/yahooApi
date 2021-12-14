package de.virnich.yahooApi

import de.virnich.yahooApi.exception.NoDataAvailableException
import de.virnich.yahooApi.model.request.HistoryRequestBuilder
import de.virnich.yahooApi.model.request.Interval
import de.virnich.yahooApi.model.request.Period
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.ZoneId

internal class YahooClientTest {

    private val yahooClient: YahooClient = YahooClient()

    @Test
    fun `get history`() {
        val response = yahooClient.getHistory(HistoryRequestBuilder("AAPL").build())

        assert(response.chart != null)
    }

    @Test
    fun `get mapped history in period`() {
        val response = yahooClient.getMappedHistory(HistoryRequestBuilder("AAPL").withPeriod(Period.FIVE_YEARS).build())

        assert(response.chart.isNotEmpty())
    }

    @Test
    fun `get mapped history of 2018`() {
        val response = yahooClient.getMappedHistory(
            HistoryRequestBuilder("AAPL")
                .withStart(LocalDate.of(2018, 1 ,1))
                .withEnd(LocalDate.of(2018, 12, 31))
                .withInterval(Interval.ONE_DAY)
                .withZoneId(ZoneId.systemDefault())
                .build()
        )

        assert(response.chart.isNotEmpty())
    }

    @Test
    fun `get mapped history in period with zoneid`() {
        val response = yahooClient.getMappedHistory(
            HistoryRequestBuilder("AAPL")
                .withPeriod(Period.TEN_YEARS)
                .withInterval(Interval.ONE_WEEK)
                .withZoneId(ZoneId.systemDefault())
                .build()
        )

        assert(response.chart.isNotEmpty())
    }

    @Test
    fun `get quote`() {

        val response = yahooClient.getQuotes("ADN1.DE")

        assert(response.quoteResponse != null)
    }

    @Test
    fun `get quotes`() {

        val response = yahooClient.getQuotes(listOf("AAPL", "ADN1.DE"))

        assert(response.quoteResponse!!.result!!.size == 2)
    }

    @Test
    fun `get quote invalid symbol`() {

        assertThrows<NoDataAvailableException> { yahooClient.getQuotes("symbol") }
    }

    @Test
    fun `get mapped history`() {

        val response = yahooClient.getMappedHistory(HistoryRequestBuilder("AAPL").build())

        assert(response.chart.isNotEmpty())
    }

    @Test
    fun search() {

        val response = yahooClient.search("apple")

        assert(response.quotes!!.isNotEmpty())
    }

    @Test
    fun `search empty term`() {

        assertThrows<IllegalArgumentException> { yahooClient.search("  ") }
    }
}
