package com.github.dontworryimmafine.dsla.acfparser

import com.github.dontworryimmafine.dsla.acfparser.exception.AcfParseException

internal object AcfTokenizer {
    sealed class Token {
        data class StringToken(
            val value: String,
        ) : Token()

        data object OpenBrace : Token()

        data object CloseBrace : Token()

        data object EOF : Token()
    }

    fun tokenize(content: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var position = 0

        while (position < content.length) {
            when (val char = content[position]) {
                '"' -> {
                    val (token, newPos) = readStringToken(content, position)
                    tokens.add(token)
                    position = newPos
                }
                '{' -> {
                    tokens.add(Token.OpenBrace)
                    position++
                }

                '}' -> {
                    tokens.add(Token.CloseBrace)
                    position++
                }

                else -> {
                    when {
                        char.isWhitespace() -> position++
                        isCommentStart(content, position) -> position = skipComment(content, position)
                        else -> position++ // Пропускаем неизвестные символы
                    }
                }
            }
        }

        tokens.add(Token.EOF)
        return tokens
    }

    private fun readStringToken(
        content: String,
        startPos: Int,
    ): Pair<Token.StringToken, Int> {
        val builder = StringBuilder()
        var pos = startPos + 1

        while (pos < content.length) {
            when (val char = content[pos]) {
                '\\' -> {
                    if (pos + 1 < content.length) {
                        builder.append(escapeChar(content[pos + 1]))
                        pos += 2
                    } else {
                        pos++
                    }
                }

                '"' -> {
                    pos++
                    return Token.StringToken(builder.toString()) to pos
                }

                else -> {
                    builder.append(char)
                    pos++
                }
            }
        }

        throw AcfParseException("Unclosed string literal")
    }

    private fun escapeChar(char: Char): Char =
        when (char) {
            'n' -> '\n'
            't' -> '\t'
            'r' -> '\r'
            '"' -> '"'
            '\\' -> '\\'
            else -> char
        }

    private fun isCommentStart(
        content: String,
        pos: Int,
    ): Boolean =
        pos + 1 < content.length &&
            content[pos] == '/' &&
            (content[pos + 1] == '/' || content[pos + 1] == '*')

    private fun skipComment(
        content: String,
        pos: Int,
    ): Int =
        when (content[pos + 1]) {
            '/' -> content.indexOf('\n', pos).let { if (it == -1) content.length else it + 1 }
            '*' -> content.indexOf("*/", pos).let { if (it == -1) content.length else it + 2 }
            else -> pos
        }
}
