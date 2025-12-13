package com.github.dontworryimmafine.dsla.acfparser.model

class AcfNode private constructor(
    private val data: Map<String, AcfValue>,
) {
    fun asMap(): Map<String, Any?> =
        data.mapValues { (_, value) ->
            when (value) {
                is AcfValue.StringValue -> value.value
                is AcfValue.ObjectValue -> AcfNode(value.children).asMap()
            }
        }

    companion object {
        fun fromMap(map: Map<String, AcfValue>): AcfNode = AcfNode(map)
    }
}
