package org.metrostate.ics.ordertrackingappkotlin

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.collections.ArrayList
import kotlin.collections.MutableList

/**
 * Represents a customer's order in the system.
 */
class Order {

    var orderID: Int = 0
        private set

    var date: Long = 0
        private set


    var totalPrice: Double = 0.0
        private set

    var type: Type? = null
        private set

    var status: Status = Status.WAITING

    var foodList: ArrayList<FoodItem> = ArrayList<FoodItem>()
        private set

    var company: String?

    /**
     * Creates an empty order with the status set to "INCOMING" and an empty food list (empty constructor).
     */
    constructor() {
        this.foodList = ArrayList<FoodItem>()
        this.status = Status.WAITING
        this.company = null
    }

    /**
     * Creates a new order with the given ID, type, date, and an initial list of food items.
     *
     * @param orderId   The unique ID of the order
     * @param type      The type of order
     * @param date      The timestamp of the order
     * @param foodList  The initial list of food items
     */
    constructor(orderId: Int, type: Type?, date: Long, foodList: MutableList<FoodItem>) {
        this.orderID = orderId
        this.type = type
        this.date = date
        this.foodList = foodList as ArrayList<FoodItem>
        this.status = Status.WAITING
        this.totalPrice = sumPrice()
        this.company = null
    }

    /**
     * Constructs an Order to restore its previous state after a failure to close the application.
     *
     * @param orderId       The unique ID of the order
     * @param date          The timestamp of the order in milliseconds
     * @param totalPrice    The total price of the order
     * @param type          The type of the order (ex: togo, delivery, or pickup)
     * @param status        The current status of the order (ex: waiting, inProgress, completed, cancelled)
     * @param foodList      The list of food items included in the order
     */
    constructor(
        orderId: Int,
        date: Long,
        totalPrice: Double,
        type: Type?,
        status: Status,
        foodList: MutableList<FoodItem>
    ) {
        this.orderID = orderId
        this.date = date
        this.totalPrice = totalPrice
        this.type = type
        this.status = status
        this.foodList = foodList as ArrayList<FoodItem>
        this.company = null
    }

    /**
     * Recalculates the total price of the current food list.
     *
     * @return The sum of all food items' prices multiplied by their quantities
     */
    fun sumPrice(): Double {
        var sum = 0.0

        // Calculates the total price of all items in the food list
        for (item in foodList) {
            sum = sum + item.price * item.quantity
        }

        return sum
    }

    /**
     * Adds a single FoodItem to the order's food list.
     *
     * @param f The FoodItem to add
     * @return  true if the item was added successfully, false if the item is null
     */
    fun addFoodItem(f: FoodItem?): Boolean {
        if (f == null) {
            return false
        }


        val priceUpdate = foodList.add(f)

        if (priceUpdate) {
            totalPrice = sumPrice()
        }

        return priceUpdate
    }

    /**
     * Returns a formatted string representing the order, including all food items.
     *
     * @return A formatted string of the order
     */
    override fun toString(): String {
        val s = StringBuilder()
        for (foodItem in foodList) {
            s.append(foodItem.toString())
        }
        val zdt = Instant.ofEpochMilli(this.date).atZone(ZoneId.of("America/Chicago"))
        val formattedDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z").format(zdt)

        return "Order #" + this.orderID + "\n" +
                formattedDate + "\n\n" +
                "Status: " + displayStatus() + '\n' +
                "Type: " + displayType() + '\n' +
                "Items: " + s + String.format("\n\nTotal Price: $%.2f", totalPrice)
    }

    /**
     * Returns the order's status as a readable string.
     *
     * @return The order's status
     */
    fun displayStatus(): String {

        return status.toString()
    }

    /**
     * Returns the order's type as a readable string.
     *
     * @return The order's type
     */
    fun displayType(): String {
        if (type == null) {
            return "No Type"
        }
        return type.toString()
    }

    companion object {
        fun parseOrderStatus(stringStatus: String?): Status? {
            if (stringStatus == null) {
                return null
            }

            try {
                return Status.valueOf(stringStatus)
            } catch (e: IllegalArgumentException) {
                System.err.println("Invalid status string: $stringStatus")
                return null
            }
        }

        fun parseOrderType(stringType: String?): Type? {
            if (stringType == null) {
                return null
            }

            try {
                return Type.valueOf(stringType)
            } catch (e: IllegalArgumentException) {
                System.err.println("Invalid type string: $stringType")
                return null
            }
        }
    }
}
