package org.metrostate.ics.ordertrackingappkotlin.parser
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.metrostate.ics.ordertrackingappkotlin.*
import org.metrostate.ics.ordertrackingappkotlin.order.FoodItem
import org.metrostate.ics.ordertrackingappkotlin.order.Status
import org.metrostate.ics.ordertrackingappkotlin.order.Type
import java.io.File

class JSONParserTest {
    @Test
    fun parse() {
        val file = File("src/test/resources/order_09-16-2025_10-00.json")

        val parser  = ParserFactory().getParser(file)
        val order = parser.parse( file)

        //assert
        /*
        check we actually parsed something
         */
        assertNotNull(order)

        val foodItemList: MutableList<FoodItem?> = ArrayList()
        foodItemList.add(FoodItem("Burger", 1, 4.39))
        foodItemList.add(FoodItem("Fries", 2, 3.09))
        foodItemList.add(FoodItem("Milkshake", 1, 5.09))
        /*
        check all attribute values of order
         */
        assertEquals(Type.TOGO, order.type )
        assertEquals(1758034800000L,order.date)

        /*
        check name, price, and quant
         */
        for (i in foodItemList.indices) {
            assertNotNull(foodItemList[i])
            assertEquals(foodItemList[i]?.name, order.foodList[i].name)
            assertEquals(foodItemList[i]?.price, order.foodList[i].price)
            assertEquals(foodItemList[i]?.quantity, order.foodList[i].quantity)
        }
        val expectedTotal = 15.66
        assertEquals(expectedTotal, order.totalPrice)
        assertEquals(Status.WAITING,order.status)
        assertEquals("FoodHub (JSON)",order.company)
    }

}