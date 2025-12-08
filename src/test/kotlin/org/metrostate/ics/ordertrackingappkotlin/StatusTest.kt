package org.metrostate.ics.ordertrackingappkotlin

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.metrostate.ics.ordertrackingappkotlin.order.Status

class StatusTest {
    @Test
    fun toStringTest() {

        //assert
        assertEquals("Cancelled", Status.CANCELLED.toString())
        assertEquals("In progress", Status.IN_PROGRESS.toString())
        assertEquals("Waiting", Status.WAITING.toString())
        assertEquals("Completed", Status.COMPLETED.toString())
    }
}