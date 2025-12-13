package com.github.dontworryimmafine.dsla.acfparser

import com.github.dontworryimmafine.dsla.acfparser.exception.AcfParseException
import com.github.dontworryimmafine.dsla.acfparser.model.AcfValue

internal class AcfSyntaxParser(
    private val tokens: List<AcfTokenizer.Token>,
) {
    private var position = 0

    fun parse(): Map<String, AcfValue> = parseObject()

    private fun parseObject(): Map<String, AcfValue> {
        val map = mutableMapOf<String, AcfValue>()

        while (true) {
            when (val token = peek()) {
                is AcfTokenizer.Token.StringToken -> {
                    val key = token.value
                    consume()

                    when (val nextToken = peek()) {
                        is AcfTokenizer.Token.StringToken -> {
                            map[key] = AcfValue.StringValue(nextToken.value)
                            consume()
                        }
                        AcfTokenizer.Token.OpenBrace -> {
                            consume()
                            map[key] = AcfValue.ObjectValue(parseObject())
                        }
                        else -> throw AcfParseException("Expected string or '{' after key '$key'")
                    }
                }
                AcfTokenizer.Token.CloseBrace -> {
                    consume()
                    return map
                }
                AcfTokenizer.Token.EOF -> return map
                else -> throw AcfParseException("Unexpected token: ${token::class.simpleName}")
            }
        }
    }

    private fun peek(): AcfTokenizer.Token = tokens[position]

    private fun consume(): AcfTokenizer.Token = tokens[position++]
}
