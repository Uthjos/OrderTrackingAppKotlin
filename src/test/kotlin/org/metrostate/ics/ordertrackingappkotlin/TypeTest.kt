package org.metrostate.ics.ordertrackingappkotlin

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.metrostate.ics.ordertrackingappkotlin.order.Type

class TypeTest {
    @Test
    fun toStringTest() {
        assertEquals("Dine-In", Type.DINE_IN.toString())
        assertEquals("To-go", Type.TOGO.toString())
        assertEquals("Delivery", Type.DELIVERY.toString())
        assertEquals("NULL TYPE", Type.DEFAULT.toString())

    }

}