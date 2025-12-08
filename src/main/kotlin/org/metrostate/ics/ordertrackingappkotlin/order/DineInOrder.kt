package org.metrostate.ics.ordertrackingappkotlin.order

import org.metrostate.ics.ordertrackingappkotlin.FoodItem
import org.metrostate.ics.ordertrackingappkotlin.Status
import org.metrostate.ics.ordertrackingappkotlin.Type

/**
 * Represents a dine-in order.
 * Dine-in orders support kitchen tips and server tips.
 */
class DineInOrder : Order {

    private var kitchenTip: Double = 0.0
    private var serverTip: Double = 0.0


    constructor() {
        this.foodList = ArrayList()
        this.status = Status.WAITING
        this.type = Type.DINE_IN
        this.company = null
    }

    /**
     * Create new order
     */
    constructor(orderId: Int, date: Long, foodList: MutableList<FoodItem>) {
        this.orderID = orderId
        this.type = Type.DINE_IN
        this.date = date
        this.foodList = foodList as ArrayList<FoodItem>
        this.status = Status.WAITING
        this.totalPrice = sumPrice()
        this.company = null
    }

    /**
     * Constructs a dine-in order to restore its previous state.
     */
    constructor(
        orderId: Int,
        date: Long,
        totalPrice: Double,
        status: Status,
        foodList: MutableList<FoodItem>,
        kitchenTip: Double = 0.0,
        serverTip: Double = 0.0
    ) {
        this.orderID = orderId
        this.date = date
        this.totalPrice = totalPrice
        this.type = Type.DINE_IN
        this.status = status
        this.foodList = foodList as ArrayList<FoodItem>
        this.company = null
        this.kitchenTip = kitchenTip
        this.serverTip = serverTip
    }

    override fun getKitchenTip(): Double = kitchenTip

    override fun setKitchenTip(amount: Double) {
        kitchenTip = amount
    }

    fun getServerTip(): Double = serverTip


    fun setServerTip(amount: Double) {
        serverTip = amount
    }

    override fun getTotalTips(): Double {
        return kitchenTip + serverTip
    }

    override fun calculateGrandTotal(): Double {
        return totalPrice + getTotalTips()
    }

    override fun toString(): String {
        return formatBaseOrder() +
                String.format("\nKitchen Tip: $%.2f", kitchenTip) +
                String.format("\nServer Tip: $%.2f", serverTip) +
                String.format("\n\nGrand Total: $%.2f", calculateGrandTotal())
    }
}

