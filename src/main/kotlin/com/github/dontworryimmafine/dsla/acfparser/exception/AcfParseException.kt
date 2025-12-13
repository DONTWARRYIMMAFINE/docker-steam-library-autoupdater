package com.github.dontworryimmafine.dsla.acfparser.exception

class AcfParseException(
    message: String,
    cause: Throwable? = null,
) : IllegalArgumentException(message, cause)
