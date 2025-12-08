package org.metrostate.ics.ordertrackingappkotlin

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FoodItemTest {
    @Test
    fun toStringTest() {
        val fItem = FoodItem("Apple",4,3.22)
        assertEquals("\n  " + 4 + "x " + "Apple" + " - " + String.format("$%.2f", 3.22) + " each", fItem.toString())
    }

}