package com.github.dontworryimmafine.dsla.model

data class DownloadUnit(
    val value: Double,
    val type: DownloadUnitType
) {
    override fun toString(): String {
        return "${"%.2f".format(value)} $type"
    }
}

enum class DownloadUnitType {
    B,
    KiB,
    MiB,
    GiB
}
