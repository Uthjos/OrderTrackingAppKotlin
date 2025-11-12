package org.metrostate.ics.ordertrackingappkotlin

/**
 * Order types.
 */
enum class Type {
    TOGO,
    PICKUP,
    DELIVERY,
    DINE_IN;

    override fun toString(): String {
        return when (this) {
            TOGO -> "To-go"
            PICKUP -> "Pickup"
            DELIVERY -> "Delivery"
            DINE_IN -> "Dine-In"
        }
    }
}