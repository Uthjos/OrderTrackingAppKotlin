package org.metrostate.ics.ordertrackingappkotlin.order

import org.metrostate.ics.ordertrackingappkotlin.FoodItem
import org.metrostate.ics.ordertrackingappkotlin.Status
import org.metrostate.ics.ordertrackingappkotlin.Type

/**
 * Represents a to-go order.
 * To-go orders only support kitchen tips.
 */
class ToGoOrder : Order {

    private var kitchenTip: Double = 0.0

    constructor() {
        this.foodList = ArrayList()
        this.status = Status.WAITING
        this.type = Type.TOGO
        this.company = null
    }

    /**
     * Create new order
     */
    constructor(orderId: Int, date: Long, foodList: MutableList<FoodItem>) {
        this.orderID = orderId
        this.type = Type.TOGO
        this.date = date
        this.foodList = foodList as ArrayList<FoodItem>
        this.status = Status.WAITING
        this.totalPrice = sumPrice()
        this.company = null
    }

    /**
     * Constructs a to-go order to restore its previous state.
     */
    constructor(
        orderId: Int,
        date: Long,
        totalPrice: Double,
        status: Status,
        foodList: MutableList<FoodItem>,
        kitchenTip: Double = 0.0
    ) {
        this.orderID = orderId
        this.date = date
        this.totalPrice = totalPrice
        this.type = Type.TOGO
        this.status = status
        this.foodList = foodList as ArrayList<FoodItem>
        this.company = null
        this.kitchenTip = kitchenTip
    }

    override fun getKitchenTip(): Double = kitchenTip

    override fun setKitchenTip(amount: Double) {
        kitchenTip = amount
    }

    override fun getTotalTips(): Double {
        return kitchenTip
    }

    override fun calculateGrandTotal(): Double {
        return totalPrice + getTotalTips()
    }

    override fun toString(): String {
        return formatBaseOrder() +
                String.format("\nKitchen Tip: $%.2f", kitchenTip) +
                String.format("\n\nGrand Total: $%.2f", calculateGrandTotal())
    }
}

