package org.metrostate.ics.ordertrackingappkotlin.parser

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.metrostate.ics.ordertrackingappkotlin.*
import org.metrostate.ics.ordertrackingappkotlin.order.FoodItem
import java.io.File

class XMLParserTest {
    @Test
    fun parse() { val file = File("src/test/resources/order_09-16-2025_21-00.xml")

        val parser  = ParserFactory().getParser(file)
        val order = parser.parse( file)

        //assert
        /*
        check we actually parsed something
         */
        assertNotNull(order)

        val foodItemList: MutableList<FoodItem?> = ArrayList()
        foodItemList.add(FoodItem("Fountain Drink", 2, 2.79))
        foodItemList.add(FoodItem("Crispy Chicken Sandwich", 2, 6.79))
        /*
        check all attribute values of order
         */
        assertEquals(Type.DINE_IN, order.type )
        assertEquals(1758074400000L,order.date)

        /*
        check name, price, and quant
         */
        for (i in foodItemList.indices) {
            assertNotNull(foodItemList[i])
            assertEquals(foodItemList[i]?.name, order.foodList[i].name)
            assertEquals(foodItemList[i]?.price, order.foodList[i].price)
            assertEquals(foodItemList[i]?.quantity, order.foodList[i].quantity)
        }
        val expectedTotal = 19.16
        assertEquals(expectedTotal, order.totalPrice)
        assertEquals(Status.WAITING,order.status)
        assertEquals("GrubStop (XML)",order.company)
    }

}