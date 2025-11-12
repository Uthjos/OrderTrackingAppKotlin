package org.metrostate.ics.ordertrackingappkotlin

/**
 * Order statuses.
 */
enum class Status {
    cancelled,
    completed,
    inProgress,
    waiting;

    override fun toString(): String {
        return when (this) {
            cancelled -> "Cancelled"
            completed -> "Completed"
            inProgress -> "In progress"
            waiting -> "Waiting"
        }
    }
}