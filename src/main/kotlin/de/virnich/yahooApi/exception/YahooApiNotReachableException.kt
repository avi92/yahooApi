package de.virnich.yahooApi.exception

class YahooApiNotReachableException(
    url: String,
    errorCause: Throwable
): RuntimeException("An error ocurred while trying to read from $url", errorCause)
