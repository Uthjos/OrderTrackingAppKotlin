package org.metrostate.ics.ordertrackingappkotlin

/**
 * Order types.
 */
enum class Type {
    togo,
    pickup,
    delivery,
    dineIn;

    override fun toString(): String {
        return when (this) {
            togo -> "To-go"
            pickup -> "Pickup"
            delivery -> "Delivery"
            dineIn -> "Dine-In"
        }
    }
}