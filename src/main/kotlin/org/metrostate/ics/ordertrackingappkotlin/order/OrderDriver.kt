package org.metrostate.ics.ordertrackingappkotlin.order

import org.json.JSONArray
import org.json.JSONObject
import org.metrostate.ics.ordertrackingappkotlin.Status
import java.io.File
import java.io.FileWriter
import java.io.IOException

/**
 * Manages a collection of orders in the system.
 * Provides methods to add, start, complete, display, and export orders.
 */
class OrderDriver {

    val orders: MutableList<Order> = ArrayList()
    private var lastCancelledOrder: Order? = null

    /**
     * Listener to watch for changes to an order's status -- in order to update the GUI when buttons are clicked.
     */
    interface OrderChangeListener {
        /**
         * Called when a new order is added.
         *
         * @param order The newly added order
         */
        fun orderAdded(order: Order?)

        /**
         * Called when an existing order is updated.
         *
         * @param order The updated order
         */
        fun orderChanged(order: Order?)
    }

    private val listeners: MutableList<OrderChangeListener> = ArrayList<OrderChangeListener>()

    /**
     * Adds a listener.
     *
     * @param l The listener to add
     */
    fun addListener(l: OrderChangeListener?) {
        if (l == null) return
        listeners.add(l)
    }

    /**
     * Removes a listener.
     *
     * @param l The listener to remove
     */
    fun removeListener(l: OrderChangeListener?) {
        listeners.remove(l)
    }

    /**
     * Notifies all listeners that a new order has been added to update the GUI.
     *
     * @param o The order that was added
     */
    private fun notifyOrderAdded(o: Order?) {
        for (l in ArrayList<OrderChangeListener>(listeners)) {
            try {
                l.orderAdded(o)
            } catch (ignored: Exception) {
            }
        }
    }

    /**
     * Notifies all listeners that an order has changed, updating the GUI.
     *
     * @param o The order that changed
     */
    private fun notifyOrderChanged(o: Order?) {
        for (l in ArrayList<OrderChangeListener>(listeners)) {
            try {
                l.orderChanged(o)
            } catch (ignored: Exception) {
            }
        }
    }

    /**
     * Adds a new order to the system.
     * The order is added to both the list of all orders and the list of incomplete orders.
     *
     * @param order The order to add
     */
    fun addOrder(order: Order?) {
        orders.add(order!!)
        notifyOrderAdded(order)
    }

    val orderCount: Int
        /**
         * Returns the total number of orders in the system.
         *
         * @return The number of orders
         */
        get() = orders.size

    /**
     * Starts an order if its status is "INCOMING".
     * Changes the status of the order to "IN PROGRESS".
     *
     * @param order The order to start
     */
    fun startOrder(order: Order) {
        // only start if it's waiting, otherwise do nothing
        if (order.status == Status.WAITING) {
            order.status = Status.IN_PROGRESS
            notifyOrderChanged(order)
        }
    }

    /**
     * Completes an order if its status is "IN PROGRESS".
     * Changes the status to "COMPLETED", removes it from incompleteOrders, and adds it to completeOrders.
     *
     * @param order The order to complete
     */
    fun completeOrder(order: Order) {
        // only complete if it's in progress, otherwise do nothing
        if (order.status == Status.IN_PROGRESS) {
            order.status = Status.COMPLETED
            notifyOrderChanged(order)
        }
    }

    /**
     * Saves all orders in the driver to JSON files in the savedOrders directory.
     *
     * @param fileDirectory The directory to save orders to
     */
    fun saveAllOrdersToJSON(fileDirectory: String) {
        for (order in orders) {
            orderExportJSON(order, fileDirectory)
        }
    }

    /**
     * Clears all orders from the system.
     */
    fun clearAllOrders() {
        orders.clear()
        lastCancelledOrder = null
    }

    /**
     * Cancels an order.
     *
     * @param order     The order to cancel
     * @return          True if the order was successfully cancelled, false otherwise
     */
    fun cancelOrderGUI(order: Order?): Boolean {
        if (order == null || order.status == Status.COMPLETED) {
            return false
        }
        order.status = Status.CANCELLED
        lastCancelledOrder = order
        notifyOrderChanged(order)
        return true
    }

    /**
     * Un-cancel a specific order (set status back to waiting) if it is currently cancelled.
     * Returns true if the order was un-cancelled, false otherwise.
     */
    fun uncancelOrder(order: Order?): Boolean {
        if (order == null) return false
        if (order.status != Status.CANCELLED) return false
        order.status = Status.WAITING
        if (lastCancelledOrder == order) {
            lastCancelledOrder = null
        }
        notifyOrderChanged(order)
        return true
    }

    companion object {
        /**
         * Exports a single order as a JSON file to the savedOrders directory.
         *
         * @param order             The order to save
         * @param fileDirectory     The folder where the JSON file will be created
         */
        fun orderExportJSON(order: Order, fileDirectory: String) {
            val OrderJSON = JSONObject()

            OrderJSON.put("orderID", order.orderID)
            OrderJSON.put("date", order.date)
            OrderJSON.put("totalPrice", order.totalPrice)
            OrderJSON.put("type", order.type)
            OrderJSON.put("status", order.status)
            OrderJSON.put("company", order.company)

            val tipJSON = JSONObject()
            tipJSON.put("kitchenTip", JSONObject().put("amount", order.getKitchenTip()))

            when (order) {
                is DeliveryOrder -> {
                    tipJSON.put("driverTip", JSONObject().put("amount", order.getDriverTip()))
                }
                is DineInOrder -> {
                    tipJSON.put("serverTip", JSONObject().put("amount", order.getServerTip()))
                }
            }
            OrderJSON.put("tip", tipJSON)

            val orderFoodsList = JSONArray()
            for (food in order.foodList) {
                val foodJSON = JSONObject()
                foodJSON.put("name", food.name)
                foodJSON.put("quantity", food.quantity)
                foodJSON.put("price", food.price)
                orderFoodsList.put(foodJSON)
            }

            OrderJSON.put("foodList", orderFoodsList)

            val fileName = "Saved_Order" + order.orderID + ".json"
            val filePath = fileDirectory + File.separator + fileName

            val fileDir = File(fileDirectory)
            if (!fileDir.exists()) {
                fileDir.mkdirs()
            }

            try {
                FileWriter(filePath).use { fw ->
                    fw.write(OrderJSON.toString(4))
                    fw.flush()
                }
            } catch (e: IOException) {
                System.err.println("Error saving order to JSON: " + e.message)
                e.printStackTrace()
            }
        }
    }
}