package org.metrostate.ics.ordertrackingappkotlin.order

/**
 * Represents a delivery order.
 * Delivery orders support kitchen tips and driver tips.
 */
class DeliveryOrder : Order {

    private var kitchenTip: Double = 0.0
    private var driverTip: Double = 0.0

    constructor() {
        this.foodList = ArrayList()
        this.status = Status.WAITING
        this.type = Type.DELIVERY
        this.company = null
    }

    /**
     * Create new order
     */
    constructor(orderId: Int, date: Long, foodList: MutableList<FoodItem>) {
        this.orderID = orderId
        this.type = Type.DELIVERY
        this.date = date
        this.foodList = foodList as ArrayList<FoodItem>
        this.status = Status.WAITING
        this.totalPrice = sumPrice()
        this.company = null
    }

    /**
     * Constructs a delivery order to restore its previous state.
     */
    constructor(
        orderId: Int,
        date: Long,
        totalPrice: Double,
        status: Status,
        foodList: MutableList<FoodItem>,
        kitchenTip: Double = 0.0,
        driverTip: Double = 0.0
    ) {
        this.orderID = orderId
        this.date = date
        this.totalPrice = totalPrice
        this.type = Type.DELIVERY
        this.status = status
        this.foodList = foodList as ArrayList<FoodItem>
        this.company = null
        this.kitchenTip = kitchenTip
        this.driverTip = driverTip
    }



    fun getDriverTip(): Double = driverTip

    fun setDriverTip(amount: Double) {
        driverTip = amount
    }

    override fun getKitchenTip(): Double {
        return kitchenTip
    }

    override fun setKitchenTip(amount: Double) {
        kitchenTip =amount
    }

    override fun getServerTip(): Double {
        return getDriverTip()
    }

    override fun setServerTip(amount: Double) {
        setDriverTip(amount)
    }

    override fun getTotalTips(): Double {
        return kitchenTip + driverTip
    }

    override fun calculateGrandTotal(): Double {
        return totalPrice + getTotalTips()
    }

    override fun toString(): String {
        return formatBaseOrder() +
                String.format("\nKitchen Tip: $%.2f", kitchenTip) +
                String.format("\nDriver Tip: $%.2f", driverTip) +
                String.format("\n\nGrand Total: $%.2f", calculateGrandTotal())
    }
}

