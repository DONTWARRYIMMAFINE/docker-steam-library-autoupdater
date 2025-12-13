package com.github.dontworryimmafine.dsla.acfparser

import com.github.dontworryimmafine.dsla.acfparser.model.AcfNode
import java.io.File

object AcfParser {
    fun parseFile(file: File): AcfNode = parseContent(file.readText())

    private fun parseContent(content: String): AcfNode {
        val tokens = AcfTokenizer.tokenize(content)
        val parser = AcfSyntaxParser(tokens)
        return AcfNode.fromMap(parser.parse())
    }
}
