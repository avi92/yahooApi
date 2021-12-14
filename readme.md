# Kotlin Client for Yahoo Finance API

<table style="border: 1px solid black;"><tr><td>

#### \*\*\* IMPORTANT LEGAL DISCLAIMER \*\*\*

---

**Yahoo!, Y!Finance, and Yahoo! finance are registered trademarks of
Yahoo, Inc.**

This project is **not** affiliated, endorsed, or vetted by Yahoo, Inc. It's
an open-source tool that uses Yahoo's publicly available APIs, and is
intended for research and educational purposes.

**You should refer to Yahoo!'s terms of use**
([here](https://policies.yahoo.com/us/en/yahoo/terms/product-atos/apiforydn/index.htm),
[here](https://legal.yahoo.com/us/en/yahoo/terms/otos/index.html), and
[here](https://policies.yahoo.com/us/en/yahoo/terms/index.htm)) **for
details on your rights to use the actual data downloaded. Remember - the
Yahoo! finance API is intended for personal use only.**

</td></tr></table>

---

This library may be used to query financial stock data from the publicly available Yahoo Finance APIs. It currently allows to query quote data, historical data and to search for quotes by name or ISIN. Please see below for examples on how to use this API.


## Examples

### Single quote

Query quote for single symbol
```kotlin
yahooClient.getQuotes("AAPL")
```
Query multiple symbols at once
```kotlin
yahooClient.getQuotes(listOf("AAPL", "GOOG"))
```

### Historical/Chart data

Simplest way to create a request for chart/history data is to use the HistoryRequestBuilder class.
```kotlin
// By default, a chart request will query daily chart data from 1970 until today.
val request = HistoryRequestBuilder("AAPL").build()
yahooClient.getHistory(request)
// get weekly chart data of last ten years 
val request = HistoryRequestBuilder("AAPL").withPeriod(Period.TEN_YEARS).withInterval(Interval.ONE_WEEK).build()
yahooClient.getHistory(request)
```
The function ``getHistory(historyRequest: HistoryRequest)`` does not perform any mapping. The returned object represents the structure as returned from the API.
However, you can use ``getMappedHistory(historyRequest: HistoryRequest)`` to get the chart data as a Map. See [History](src/main/kotlin/de/virnich/yahooApi/model/History.kt).
```kotlin
val request = HistoryRequestBuilder("AAPL").build()
yahooClient.getMappedHistory(request)
```

### Search quotes

Search for quotes
```kotlin
// search by name
yahooClient.search("Apple Inc.")
// search by ISIN
yahooClient.search("US0378331005")
```
