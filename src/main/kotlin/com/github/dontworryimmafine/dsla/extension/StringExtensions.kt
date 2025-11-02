package com.github.dontworryimmafine.dsla.extension

import com.github.dontworryimmafine.dsla.model.Progressible
import java.util.regex.Pattern

private val progressiblePattern = Pattern.compile("progress: \\d+\\.\\d+ \\((\\d+) / (\\d+)\\)")

fun String.toProgressible(): Progressible {
    val matcher = progressiblePattern.matcher(this)

    if (matcher.find()) {
        try {
            val current = matcher.group(1).toDouble()
            val total = matcher.group(2).toDouble()
            return Progressible(current, total)
        } catch (_: NumberFormatException) {
            return Progressible()
        }
    }
    return Progressible()
}
