package org.metrostate.ics.ordertrackingappkotlin.parser

import java.io.File

class ParserFactory(
) {
    var parser: Parser? = null


    fun getParser(file: File): Parser {
        if (file.path.endsWith(".json")) {
            parser = JSONParser()
        }
        if (file.path.endsWith(".xml")) {
            parser = XMLParser()
        }
        println("Parser is : $parser")
        println("File is : $file" )

        return parser!!
    }
}
