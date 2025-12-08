package org.metrostate.ics.ordertrackingappkotlin.directory

import org.metrostate.ics.ordertrackingappkotlin.order.OrderDriver
import java.nio.file.Paths
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SaveState {

    companion object{
        /**
         * Saves all orders to historyOrder Directory
         * @param orderDriver
         */
        fun saveStateOnExit(orderDriver : OrderDriver){
            val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            var savePath = Paths.get(Directory.getDirectory(Directory.historyOrders))
                .resolve(today)
            Directory.createDirectory(savePath.toString())

            orderDriver.saveAllOrdersToJSON(savePath.toString())

            Directory.deleteFilesInDirectory(Directory.savedOrders)
            Directory.deleteFilesInDirectory(Directory.importOrders)
        }
    }

}