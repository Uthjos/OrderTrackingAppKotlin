package org.metrostate.ics.ordertrackingappkotlin.order

import org.metrostate.ics.ordertrackingappkotlin.order.Status
import org.metrostate.ics.ordertrackingappkotlin.order.Type
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Abstract base class representing a customer's order.
 * Concrete order types should extend this class and implement TippableOrder.
 */
abstract class Order : TippableOrder {

    var orderID: Int = 0
        protected set

    var date: Long = 0
        protected set

    var totalPrice: Double = 0.0
        protected set

    var type: Type = Type.DEFAULT
        protected set

    var status: Status = Status.WAITING

    var foodList: ArrayList<FoodItem> = ArrayList<FoodItem>()
        protected set

    var company: String? = null


    /**
     * Recalculates the total price of the current food list.
     *
     * @return The sum of all food items' prices multiplied by their quantities
     */
    fun sumPrice(): Double {
        var sum = 0.0

        // Calculates the total price of all items in the food list
        for (item in foodList) {
            sum += item.price * item.quantity
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
     * Subclasses should override this to include their specific tip information.
     *
     * @return A formatted string of the order
     */
    fun formatBaseOrder(): String {
        val s = StringBuilder()
        for (foodItem in foodList) {
            s.append(foodItem.toString())
        }
        val zdt = Instant.ofEpochMilli(this.date).atZone(ZoneId.of("America/Chicago"))
        val formattedDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z").format(zdt)

        return "Order #$orderID\n" +
                "$formattedDate\n\n" +
                "Status: $status\n" +
                "Type: $type\n" +
                "Items:$s" +
                String.format("\n\nTotal Price: $%.2f", totalPrice)
    }
}