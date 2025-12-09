package org.metrostate.ics.ordertrackingappkotlin.order

/**
 * Interface for orders that support tipping functionality.
 * Implementing classes should provide their own tip management based on order type.
 */
interface TippableOrder {

    fun getKitchenTip(): Double

    fun setKitchenTip(amount: Double)

    fun getServerTip(): Double

    fun setServerTip(amount: Double)

    fun getTotalTips(): Double

    fun calculateGrandTotal(): Double
}

