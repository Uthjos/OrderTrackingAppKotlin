package org.metrostate.ics.ordertrackingappkotlin.directory

import org.metrostate.ics.ordertrackingappkotlin.order.OrderDriver

class SaveState {

    companion object{
        /**
         * Saves all orders to historyOrder Directory
         * @param orderDriver
         */
        fun saveStateOnExit(orderDriver : OrderDriver){
            orderDriver.saveAllOrdersToJSON(Directory.getDirectory(Directory.savedOrders))

            Directory.deleteFilesInDirectory(Directory.importOrders)
        }
    }

}