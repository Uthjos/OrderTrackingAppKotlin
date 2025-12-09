package org.metrostate.ics.ordertrackingappkotlin.parser

import java.io.File

class ParserFactory(
) {
    var parser: Parser? = null

    /**
     * provide correct parser by file extension
     */
    fun getParser(file: File): Parser {
        if (file.path.endsWith(".json")) {
            if (file.name.startsWith("Saved_Order")) {//check if saved json or new one
                parser = SavedJSONParser()
            } else {
                parser = JSONParser()
            }
        }
        if (file.path.endsWith(".xml")) {
            parser = XMLParser()
        }
        println("Parser is : $parser")
        println("File is : $file" )

        return parser!!
    }
}
