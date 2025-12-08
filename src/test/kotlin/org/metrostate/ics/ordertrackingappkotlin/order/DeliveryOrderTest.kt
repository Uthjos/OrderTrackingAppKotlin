package org.metrostate.ics.ordertrackingappkotlin.order

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DeliveryOrderTest {
    @Test
    fun calculateGrandTotal() {
        val testDeliveryOrder = DeliveryOrder()
        testDeliveryOrder.addFoodItem(FoodItem("Apple",10,4.33))
        testDeliveryOrder.setKitchenTip(1.00)
        testDeliveryOrder.setDriverTip(5.00)

        val expectedValue= 49.3
        assertEquals(expectedValue, testDeliveryOrder.calculateGrandTotal())
    }

    @Test
    fun toStringTest() {
        val testDeliveryOrder = DeliveryOrder()
        testDeliveryOrder.addFoodItem(FoodItem("Apple",10,4.33))
        testDeliveryOrder.setKitchenTip(1.00)
        testDeliveryOrder.setDriverTip(5.00)

        assertEquals("Order #0\n" +
                "1969-12-31 18:00 CST\n" +
                "\n" +
                "Status: Waiting\n" +
                "Type: Delivery\n" +
                "Items:\n" +
                "  10x Apple - \$4.33 each\n" +
                "\n" +
                "Total Price: \$43.30\n" +
                "Kitchen Tip: \$1.00\n" +
                "Driver Tip: \$5.00\n" +
                "\n" +
                "Grand Total: \$49.30",testDeliveryOrder.toString())

    }

}