package de.virnich.yahooApi.exception

class NoDataAvailableException(symbol: String) : RuntimeException("No data available for symbol(s): `$symbol`")
