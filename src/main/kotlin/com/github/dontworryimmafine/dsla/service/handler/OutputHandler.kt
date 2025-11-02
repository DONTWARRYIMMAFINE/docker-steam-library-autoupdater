package com.github.dontworryimmafine.dsla.service.handler

import com.github.dontworryimmafine.dsla.model.ResultMessage

interface OutputHandler {
    val next: OutputHandler?

    fun handle(output: List<String>): ResultMessage?
    fun proceed(output: List<String>): ResultMessage? = next?.handle(output)
}
