package de.virnich.yahooApi

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.yahoo.finance.query1.models.QuoteResponse
import com.yahoo.finance.query1.models.SearchResponse
import com.yahoo.finance.query2.models.HistoryResponse
import de.virnich.yahooApi.exception.NoDataAvailableException
import de.virnich.yahooApi.exception.YahooApiNotReachableException
import de.virnich.yahooApi.model.History
import de.virnich.yahooApi.model.HistoryMapper
import de.virnich.yahooApi.model.request.HistoryRequest
import de.virnich.yahooApi.model.request.HistoryRequestBuilder
import org.apache.commons.lang3.StringUtils
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

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

    private val httpClient: HttpClient = HttpClient.newHttpClient()
    private val objectMapper: ObjectMapper =
        jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    private val historyMapper: HistoryMapper = HistoryMapper()

    /**
     * Returns a [QuoteResponse] for the requested symbols.
     *
     * @param symbols Symbols to fetch the quote for.
     * @return [QuoteResponse] Data as returned by the Yahoo API.
     *
     * @throws IllegalArgumentException if there was no input symbol provided
     * @throws NoDataAvailableException if there is no data available for the requested symbol
     * @throws YahooApiNotReachableException if there is a problem receiving the data
     */
    fun getQuotes(symbols: List<String>): QuoteResponse {

        if (symbols.isEmpty()) {
            throw IllegalArgumentException("You have to provide at least one symbol!")
        }

        val symbolParameter = symbols.joinToString(",")
        val url = "${Constants.YAHOO_QUOTE_URL}quote?symbols=$symbolParameter"

        try {
            val request = HttpRequest.newBuilder(URI(url)).build()
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

            val quoteResponse: QuoteResponse = handleQuoteResponse(response)
            if (quoteResponse.quoteResponse?.result!!.isEmpty()) {
                throw NoDataAvailableException(symbolParameter)
            }
            return quoteResponse
        } catch (ex: IOException) {
            throw YahooApiNotReachableException(url, ex)
        }
    }

    /**
     * Returns a [QuoteResponse] for the requested symbol.
     *
     * @param symbol Symbol to fetch the quote for.
     * @return [QuoteResponse] Data as returned by the Yahoo API.
     *
     * @throws IllegalArgumentException if there was no input symbol provided
     * @throws NoDataAvailableException if there is no data available for the requested symbol
     * @throws YahooApiNotReachableException if there is a problem receiving the data
     */
    fun getQuotes(symbol: String): QuoteResponse {

        if (StringUtils.isBlank(symbol)) {
            throw IllegalArgumentException("Symbol cannot be empty!")
        }

        return getQuotes(listOf(symbol))
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

        try {
            val request = HttpRequest.newBuilder(URI(url)).GET().build()
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            val historyResponse: HistoryResponse = handleHistoryResponse(response)

            if (historyResponse.chart?.result == null || historyResponse.chart.result.isEmpty()) {
                throw NoDataAvailableException(historyRequest.symbol)
            }

            return historyResponse
        } catch (ex: IOException) {
            throw YahooApiNotReachableException(url, ex)
        }
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
        try {

            val request = HttpRequest.newBuilder(URI(url)).GET().build()
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

            return handleSearchResponse(response)
        } catch (ex: IOException) {
            throw YahooApiNotReachableException(url, ex)
        }
    }

    private fun handleHistoryResponse(response: HttpResponse<String>): HistoryResponse {

        val historyResponse: HistoryResponse = objectMapper.readValue(response.body())
        if (response.statusCode() == 200) {
            return historyResponse
        } else {
            val errorMessage = historyResponse.chart?.error?.description ?: ""
            throw RuntimeException("Error querying data from ${response.request().uri()}. Error message: $errorMessage")
        }
    }

    private fun handleQuoteResponse(response: HttpResponse<String>): QuoteResponse {

        val quoteResponse: QuoteResponse = objectMapper.readValue(response.body())
        if (response.statusCode() == 200) {
            return quoteResponse
        } else {
            val errorMessage = quoteResponse.quoteResponse?.error?.description ?: ""
            throw RuntimeException("Error querying data from ${response.request().uri()}. Error message: $errorMessage")
        }
    }

    private fun handleSearchResponse(response: HttpResponse<String>): SearchResponse {

        val searchResponse: SearchResponse = objectMapper.readValue(response.body())
        if (response.statusCode() == 200) {
            return searchResponse
        } else {
            throw RuntimeException("Error querying data from ${response.request().uri()}!")
        }
    }
}
