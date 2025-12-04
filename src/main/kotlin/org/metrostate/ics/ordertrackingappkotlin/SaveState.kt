package org.metrostate.ics.ordertrackingappkotlin

class SaveState {

    /**
     * Saves all orders to historyOrder Directory
     * @param orderDriver
     */
    fun saveStateOnExit(orderDriver :OrderDriver){
        var historyOrdersPath: String = Directory.getDirectory(Directory.historyOrders)
        var saveOrderPath: String = Directory.getDirectory(Directory.savedOrders)
        var importPath: String = Directory.getDirectory(Directory.importOrders)

        orderDriver.saveAllOrdersToJSON(historyOrdersPath)
        Directory.deleteFilesInDirectory(saveOrderPath)
        Directory.deleteFilesInDirectory(importPath)
    }


}
