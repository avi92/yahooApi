package de.virnich.yahooApi

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.yahoo.finance.query1.models.Quote
import com.yahoo.finance.query1.models.QuoteResponse
import com.yahoo.finance.query1.models.SearchResponse
import com.yahoo.finance.query2.models.HistoryResponse
import de.virnich.yahooApi.exception.NoDataAvailableException
import de.virnich.yahooApi.exception.YahooApiNotReachableException
import de.virnich.yahooApi.model.History
import de.virnich.yahooApi.model.HistoryMapper
import de.virnich.yahooApi.model.request.HistoryRequest
import de.virnich.yahooApi.model.request.HistoryRequestBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.lang3.StringUtils
import java.io.IOException

/**
 * [YahooClient] is a lightweight Wrapper for the Yahoo Finance API. It may be used to query current quotes or
 * historical quote data. It also provides a simple method to search for quotes available on Yahoo Finance API.
 *
 * This client provides the data as Kotlin classes, maintaining the structure as it is returned from the Yahoo Finance
 * API. Single exception is the method [getMappedHistory], which performs a mapping to [History], returning the chart
 * history using the [Map] interface.
 *
 * All data returned is provided by Yahoo, make sure to check the [Terms of Service][https://legal.yahoo.com/].
 */
class YahooClient {

    private val okHttpClient: OkHttpClient = OkHttpClient()
    private val objectMapper: ObjectMapper =
        jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    private val historyMapper: HistoryMapper = HistoryMapper()

    /**
     * Returns a [QuoteResponse] for the requested symbols.
     *
     * @param symbols Symbols to fetch the quote for.
     * @return [List]<[Quote]> Data as returned by the Yahoo API.
     *
     * @throws IllegalArgumentException if there was no input symbol provided
     * @throws NoDataAvailableException if there is no data available for the requested symbol
     * @throws YahooApiNotReachableException if there is a problem receiving the data
     */
    fun getQuote(symbols: List<String>): List<Quote> {

        if (symbols.isEmpty()) {
            throw IllegalArgumentException("You have to provide at least one symbol!")
        }

        val symbolParameter = symbols.joinToString(",")
        val url = "${Constants.YAHOO_QUOTE_URL}quote?symbols=$symbolParameter"

        val json = getJson(url)
        val quoteResponse: QuoteResponse = objectMapper.readValue(json)

        val error = quoteResponse.quoteResponse?.error
        if (error != null) {
            // TODO: test and document; replace by custom exception
            throw RuntimeException("${error.code} ${error.description}")
        } else if (quoteResponse.quoteResponse?.result == null || quoteResponse.quoteResponse.result.isEmpty()) {
            throw NoDataAvailableException(symbolParameter)
        } else {
            return quoteResponse.quoteResponse.result
        }
    }

    /**
     * Returns a [QuoteResponse] for the requested symbol.
     *
     * @param symbol Symbol to fetch the quote for.
     * @return [Quote] Data as returned by the Yahoo API.
     *
     * @throws IllegalArgumentException if there was no input symbol provided
     * @throws NoDataAvailableException if there is no data available for the requested symbol
     * @throws YahooApiNotReachableException if there is a problem receiving the data
     */
    fun getQuote(symbol: String): Quote {

        if (StringUtils.isBlank(symbol)) {
            throw IllegalArgumentException("Symbol cannot be empty!")
        }

        return getQuote(listOf(symbol))[0]
    }

    /**
     * Returns a [HistoryResponse] as requested. For query parameters see [HistoryRequest]. Simplest way to create
     * a [HistoryRequest] is to use a [HistoryRequestBuilder].
     *
     * @param historyRequest The query parameters.
     * @return [HistoryResponse] Data as returned by the Yahoo API.
     *
     * @throws NoDataAvailableException if there is no data available for the requested symbol
     * @throws YahooApiNotReachableException if there is a problem receiving the data
     */
    fun getHistory(historyRequest: HistoryRequest): HistoryResponse {

        val url = "${Constants.YAHOO_CHART_URL}chart/${historyRequest.symbol}?${historyRequest.toParameterString()}"

        val json = getJson(url)
        val historyResponse: HistoryResponse = objectMapper.readValue(json)

        if (historyResponse.chart?.result == null || historyResponse.chart.result.isEmpty()) {
            throw NoDataAvailableException(historyRequest.symbol)
        }

        return historyResponse
    }

    /**
     * Returns chart [History]. This method returns the same data as [getHistory], but performs a mapping from
     * [HistoryResponse] to [History], which uses the [Map] interface to provide the chart data.
     *
     * @param historyRequest The query parameters.
     * @return [History] Yahoo chart data mapped to custom POJO.
     *
     * @throws NoDataAvailableException if there is no data available for the requested symbol
     * @throws YahooApiNotReachableException if there is a problem receiving the data
     */
    fun getMappedHistory(historyRequest: HistoryRequest): History {

        val historyResponse = getHistory(historyRequest)
        return historyMapper.mapYahooChartToHistory(historyResponse.chart!!.result!![0])
    }

    /**
     * Search for quotes available via Yahoo Finance.
     *
     * @param term The Search term
     * @return [SearchResponse]
     * @throws IllegalArgumentException if search term is empty
     * @throws YahooApiNotReachableException if there is a problem receiving the data
     */
    fun search(term: String): SearchResponse {

        if (StringUtils.isBlank(term)) {
            throw IllegalArgumentException("Search term cannot be empty.")
        }

        val url = "${Constants.YAHOO_SEARCH_URL}search?q=$term&newsCount=0"

        val json = getJson(url)
        return objectMapper.readValue(json)
    }

    private fun getJson(url: String): String {

        val request = Request.Builder()
            .url(url)
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            return response.body!!.string()
        } catch (ex: IOException) {
            throw YahooApiNotReachableException(url, ex)
        }
    }
}
