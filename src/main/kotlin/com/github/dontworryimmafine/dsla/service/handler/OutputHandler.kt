package com.github.dontworryimmafine.dsla.service.handler

import com.github.dontworryimmafine.dsla.model.ResultMessage

interface OutputHandler {
    fun handle(output: List<String>): ResultMessage?
}
