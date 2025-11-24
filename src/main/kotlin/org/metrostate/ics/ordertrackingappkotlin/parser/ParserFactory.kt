package org.metrostate.ics.ordertrackingappkotlin.parser

class ParserFactory(
)
fun getParser(file: String): Parser {
    if (file.endsWith(".json")) {
        return JSONParser()
    }
    if (file.endsWith(".xml")){
        return XMLParser()
    }
    throw IllegalArgumentException("File does not end with '.json' or '.xml'.")
}
