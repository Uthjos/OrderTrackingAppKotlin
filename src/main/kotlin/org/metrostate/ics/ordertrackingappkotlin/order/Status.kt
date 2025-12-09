package org.metrostate.ics.ordertrackingappkotlin.order

/**
 * Order statuses Enum class
 */
enum class Status(val color: String) {
    CANCELLED("#c62828"),   // Red
    COMPLETED("#2e7d32"),   // Green
    IN_PROGRESS("#1565c0"), // Blue
    WAITING("#fb8c00");     // Orange

    override fun toString(): String {
        return when (this) {
            CANCELLED -> "Cancelled"
            COMPLETED -> "Completed"
            IN_PROGRESS -> "In progress"
            WAITING -> "Waiting"
        }
    }
}