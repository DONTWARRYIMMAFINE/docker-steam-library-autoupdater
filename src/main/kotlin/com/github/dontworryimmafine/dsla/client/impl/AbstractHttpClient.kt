package com.github.dontworryimmafine.dsla.client.impl

import com.github.dontworryimmafine.dsla.exception.HttpClientException
import com.github.dontworryimmafine.dsla.exception.HttpServerException
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate

abstract class AbstractHttpClient(
    private val restTemplate: RestTemplate,
) {
    fun <T> get(
        url: String,
        responseType: Class<T>,
        uriVariables: Map<String, Any> = emptyMap(),
        headers: HttpHeaders = HttpHeaders(),
    ): ResponseEntity<T> {
        return executeRequest(url, HttpMethod.GET, null, responseType, uriVariables, headers)
    }

    private fun <T, R> executeRequest(
        url: String,
        method: HttpMethod,
        body: R?,
        responseType: Class<T>,
        uriVariables: Map<String, Any> = emptyMap(),
        headers: HttpHeaders = HttpHeaders(),
    ): ResponseEntity<T> {
        try {
            return restTemplate.exchange(url, method, HttpEntity(body, headers), responseType, uriVariables)
        } catch (ex: HttpClientErrorException) {
            throw HttpClientException(
                "HTTP Client exception for url $url and method $method. " +
                        "Status code: ${ex.statusCode.value()}"
            )
        } catch (ex: HttpServerErrorException) {
            throw HttpServerException(
                "HTTP Server exception for url $url and method $method. " +
                        "Status code: ${ex.statusCode.value()}"
            )
        } catch (ex: ResourceAccessException) {
            throw HttpServerException(
                "ResourceAccessException for url $url and method $method. " +
                        "Cause: ${ex.message}"
            )
        }
    }
}
