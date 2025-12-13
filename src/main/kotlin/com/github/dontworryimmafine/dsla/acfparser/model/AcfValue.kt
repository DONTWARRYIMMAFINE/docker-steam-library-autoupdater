package com.github.dontworryimmafine.dsla.acfparser.model

sealed interface AcfValue {
    data class StringValue(
        val value: String,
    ) : AcfValue

    data class ObjectValue(
        val children: Map<String, AcfValue>,
    ) : AcfValue
}
