package com.github.dontworryimmafine.dsla.config.properties

class HttpClientProperties(
    var baseUrl: String = "",
    var paths: Map<String, String> = mapOf(),
) {
    fun getEndpoint(pathName: String): String = paths[pathName] ?: throw IllegalArgumentException("Endpoint [$pathName] not defined")
}
