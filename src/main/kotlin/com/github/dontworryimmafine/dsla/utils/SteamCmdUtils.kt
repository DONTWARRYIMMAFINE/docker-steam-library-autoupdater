package com.github.dontworryimmafine.dsla.utils

import com.github.dontworryimmafine.dsla.model.MessageType
import com.github.dontworryimmafine.dsla.model.ProcessProgress
import com.github.dontworryimmafine.dsla.model.ProcessType
import com.github.dontworryimmafine.dsla.model.ResultMessage
import java.util.regex.Pattern

object SteamCmdUtils {
    private val processPattern = Pattern.compile("progress: \\d+\\.\\d+ \\((\\d+) / (\\d+)\\)")

    fun extractResultMessage(output: List<String>): ResultMessage {
        val errorMessage = extractErrorMessage(output)
        if (errorMessage != null) {
            return ResultMessage(errorMessage, MessageType.ERROR)
        }

        val successMessage = extractSuccessMessage(output)
        if (successMessage != null) {
            return ResultMessage(successMessage, MessageType.SUCCESS)
        }

        return ResultMessage(output.last(), MessageType.INFO)
    }

    fun isProcessMessage(line: String?): Boolean {
        return line?.contains("progress", ignoreCase = true) == true
    }

    fun determineProcessType(line: String): ProcessType {
        return when {
            line.contains("downloading", ignoreCase = true) -> ProcessType.DOWNLOADING
            line.contains("verifying", ignoreCase = true) -> ProcessType.VALIDATING
            else -> ProcessType.UNSATISFIED
        }
    }

    fun parseProcessMessage(line: String): ProcessProgress {
        val matcher = processPattern.matcher(line)

        if (matcher.find()) {
            try {
                val current = matcher.group(1).toDouble()
                val total = matcher.group(2).toDouble()
                return ProcessProgress(current, total)
            } catch (_: NumberFormatException) {
                // Ignore incorrect numbers
            }
        }
        return ProcessProgress()
    }

    fun isNoCredentialCache(output: String): Boolean {
        return output.contains("no cached credentials", ignoreCase = true)
    }

    private fun extractErrorMessage(output: List<String>): String? {
        return output.lastOrNull {
            it.contains("error", ignoreCase = true) ||
            it.contains("failed", ignoreCase = true) ||
            it.contains("fail", ignoreCase = true)
        }
    }

    private fun extractSuccessMessage(output: List<String>): String? {
        return output.lastOrNull {
            it.contains("success", ignoreCase = true)
        }
    }
}
