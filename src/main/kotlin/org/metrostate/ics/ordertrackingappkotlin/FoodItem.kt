package org.metrostate.ics.ordertrackingappkotlin

/**
 * Represents a food item in an order.
 * Contains the item's name, quantity, and price.
 */
class FoodItem (
    var name: String = "Null",
    var quantity: Int = 0,
    var price: Double = 0.0
    ){

    /**
     * Returns a formatted string representing the food item.
     * Includes the quantity, name, and price per item.
     *
     * @return Formatted string of the food item
     */
    override fun toString(): String {
        return "\n  " + quantity + "x " + name + " - " + String.format("$%.2f", price) + " each"
    }
}