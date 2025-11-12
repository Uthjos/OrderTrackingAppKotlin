package org.metrostate.ics.ordertrackingappkotlin

/**
 * Order statuses.
 */
enum class Status {
    CANCELLED,
    COMPLETED,
    IN_PROGRESS,
    WAITING;

    override fun toString(): String {
        return when (this) {
            CANCELLED -> "Cancelled"
            COMPLETED -> "Completed"
            IN_PROGRESS -> "In progress"
            WAITING -> "Waiting"
        }
    }
}