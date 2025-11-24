package org.metrostate.ics.ordertrackingappkotlin.parser

class ParserFactory(
) {
    var parser: Parser? = null


    fun getParser(file: String): Parser {
        if (file.endsWith(".json")) {
            parser = JSONParser()
        }
        if (file.endsWith(".xml")) {
            parser = XMLParser()
        }
        return parser!!
    }
}
