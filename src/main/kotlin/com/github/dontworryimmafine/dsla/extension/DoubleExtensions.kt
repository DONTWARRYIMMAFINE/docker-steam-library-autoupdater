package com.github.dontworryimmafine.dsla.extension

import com.github.dontworryimmafine.dsla.model.DownloadUnit
import com.github.dontworryimmafine.dsla.model.DownloadUnitType

fun Double.toDownloadUnit(targetType: DownloadUnitType? = null): DownloadUnit {
    return when (targetType) {
        DownloadUnitType.B -> DownloadUnit(this, DownloadUnitType.B)
        DownloadUnitType.KiB -> DownloadUnit(this / 1024, DownloadUnitType.KiB)
        DownloadUnitType.MiB -> DownloadUnit(this / (1024 * 1024), DownloadUnitType.MiB)
        DownloadUnitType.GiB -> DownloadUnit(this / (1024 * 1024 * 1024), DownloadUnitType.GiB)
        null -> {
            when {
                this >= 1024L * 1024 * 1024 -> DownloadUnit(this / (1024L * 1024 * 1024), DownloadUnitType.GiB)
                this >= 1024L * 1024 -> DownloadUnit(this / (1024L * 1024), DownloadUnitType.MiB)
                this >= 1024 -> DownloadUnit(this / 1024, DownloadUnitType.KiB)
                else -> DownloadUnit(this, DownloadUnitType.B)
            }
        }
    }
}
