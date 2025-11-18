package org.metrostate.ics.ordertrackingappkotlin

/**
 * Order types.
 */
enum class Type(val color: String) {
    TOGO("#6a1b9a"),        // Purple
    PICKUP("#2e7d32"),      // Green
    DELIVERY("#1565c0"),    // Blue
    DINE_IN("#bdba13");     // Yellow

    override fun toString(): String {
        return when (this) {
            TOGO -> "To-go"
            PICKUP -> "Pickup"
            DELIVERY -> "Delivery"
            DINE_IN -> "Dine-In"
        }
    }
}