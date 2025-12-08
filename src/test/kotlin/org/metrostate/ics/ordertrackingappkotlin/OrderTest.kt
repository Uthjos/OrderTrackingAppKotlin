package org.metrostate.ics.ordertrackingappkotlin

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.metrostate.ics.ordertrackingappkotlin.order.DineInOrder
import org.metrostate.ics.ordertrackingappkotlin.order.FoodItem
import org.metrostate.ics.ordertrackingappkotlin.order.Order
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class OrderTest {
    var order: Order? = null
    @BeforeEach
    fun setUp() {
        //arrange set of food items
        val foodItem1 = FoodItem("Apple", 1, 1.5)
        val foodItem2 = FoodItem("Orange", 2, 2.30)
        val foodItem3 = FoodItem("Taco", 3, 3.99)
        val foodItem4 = FoodItem("Milk", 4, 4.10)

        //arrange new test order with food items
        val foodList = mutableListOf<FoodItem>()
        foodList.add(foodItem1)
        foodList.add(foodItem2)
        foodList.add(foodItem3)
        foodList.add(foodItem4)
        order = DineInOrder(1,1758034800000,foodList)
    }
    @Test
    fun sumPrice() {
        assertNotNull(order)
        assertEquals(34.47, order?.totalPrice)
    }

    @Test
    fun addFoodItem() {
        assertNotNull(order)

        val foodItem1 = FoodItem("Apple", 1, 1.5)
        order?.addFoodItem(foodItem1)
        assertTrue(order?.foodList?.contains(foodItem1) == true)
    }

    @Test
    fun toStringTest() {
        assertNotNull(order)
        val expectedString ="Order #1\n" +
                "2025-09-16 10:00 CDT\n" +
                "\n" +
                "Status: Waiting\n" +
                "Type: Dine-In\n" +
                "Items:\n" +
                "  1x Apple - \$1.50 each\n" +
                "  2x Orange - \$2.30 each\n" +
                "  3x Taco - \$3.99 each\n" +
                "  4x Milk - \$4.10 each\n" +
                "\n" +
                "Total Price: \$34.47\n" +
                "Kitchen Tip: \$0.00\n" +
                "Server Tip: \$0.00\n" +
                "\n" +
                "Grand Total: \$34.47"
        assertEquals(expectedString, order.toString())
    }

}