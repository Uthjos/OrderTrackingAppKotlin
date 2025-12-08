package org.metrostate.ics.ordertrackingappkotlin

/**
 * Order types.
 */
enum class Type(val color: String) {
    TOGO("#6a1b9a"),        // Purple
    DELIVERY("#1565c0"),    // Blue
    DINE_IN("#bdba13"),     // Yellow
    DEFAULT("#808080");     // Grey

    override fun toString(): String {
        return when (this) {
            TOGO -> "To-go"
            DELIVERY -> "Delivery"
            DINE_IN -> "Dine-In"
            DEFAULT -> "NULL TYPE"
        }
    }
}