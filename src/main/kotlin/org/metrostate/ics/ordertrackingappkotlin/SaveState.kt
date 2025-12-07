package org.metrostate.ics.ordertrackingappkotlin

class SaveState {

    companion object{
        /**
         * Saves all orders to historyOrder Directory
         * @param orderDriver
         */
        fun saveStateOnExit(orderDriver :OrderDriver){
//            var historyOrdersPath: String = Directory.getDirectory(Directory.historyOrders)
//            var saveOrderPath: String = Directory.getDirectory(Directory.savedOrders)
//            var importPath: String = Directory.getDirectory(Directory.importOrders)

//            orderDriver.saveAllOrdersToJSON(historyOrdersPath)
//            Directory.deleteFilesInDirectory(saveOrderPath)
//            Directory.deleteFilesInDirectory(importPath)

            orderDriver.saveAllOrdersToJSON(Directory.getDirectory(Directory.historyOrders))

            Directory.deleteFilesInDirectory(Directory.savedOrders)
            Directory.deleteFilesInDirectory(Directory.importOrders)
        }
    }

}
