package org.metrostate.ics.ordertrackingappkotlin.parser

import org.json.JSONObject
import org.json.JSONTokener
import org.metrostate.ics.ordertrackingappkotlin.FoodItem
import org.metrostate.ics.ordertrackingappkotlin.Order
import org.metrostate.ics.ordertrackingappkotlin.Status
import org.metrostate.ics.ordertrackingappkotlin.Type
import org.metrostate.ics.ordertrackingappkotlin.parser.Parser.Companion.nextOrderNumber
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.SAXException
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.ArrayList
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

interface Parser {
    @Throws(IOException::class)
    fun parse(file: File) : Order

    companion object {
        @JvmStatic
        var nextOrderNumber = 1
        /**
         * Static helper method
         * returns next order number and increments the counter
         * @return int, next Order ID number
         */
        get() = field++
        /**
         * set method that should only be called on when recalling current state
         * from a failed closure, and adds 1.
         * @param orderID   The maximum OrderID from current state
         */
        set(orderID) {
            field = orderID + 1
        }
    }
}

class JSONParser : Parser {
    override fun parse(file: File) : Order {
        val orderDate: Long
        val orderType: Type?
        val foodItemList: MutableList<FoodItem> = ArrayList<FoodItem>()

        FileReader(file).use { fr ->
            val jsonObject = JSONObject(JSONTokener(fr))
            val orderJson = jsonObject.getJSONObject("order")
            val orderDateObj = orderJson.get("order_date")
            if (orderDateObj is Number) orderDate = orderDateObj.toLong()
            else orderDate = orderDateObj.toString().toLong()


            val typeStr = orderJson.getString("type")
            orderType = Type.valueOf(typeStr.uppercase(Locale.getDefault()))

            val itemArray = orderJson.getJSONArray("items")
            for (o in itemArray) {
                val item = o as JSONObject
                val quantity = (item.get("quantity") as Number).toInt()
                val price = (item.get("price") as Number).toDouble()
                val name = item.getString("name")
                foodItemList.add(FoodItem(name, quantity, price))
            }
        }
        val order = Order(
            nextOrderNumber, orderType!!, orderDate, foodItemList
        )
        order.company = "FoodHub (JSON)"
        return order

    }
}

class XMLParser : Parser {
    override fun parse(file: File) : Order {
        var orderDate: Long = 0
        var orderType: Type? = null
        val foodItemList: MutableList<FoodItem> = ArrayList<FoodItem>()

        try {
            val dbFactory = DocumentBuilderFactory.newInstance()
            val dBuilder = dbFactory.newDocumentBuilder()
            val doc = dBuilder.parse(file)
            doc.documentElement.normalize()

            //Reads and grabs order Date and Type
            val nList = doc.getElementsByTagName("Order")
            for (i in 0..<nList.length) {
                val node = nList.item(i)
                if (node.nodeType == Node.ELEMENT_NODE) {
                    val eElement = node as Element
                    orderDate = eElement.getAttribute("id").toLong()
                    val typeStr = eElement.getElementsByTagName("OrderType").item(0).textContent
                    orderType = Type.valueOf(typeStr.uppercase(Locale.getDefault()))
                }
            }

            //Reads and creates FoodItems for foodItemList
            val nodeList = doc.getElementsByTagName("Item")
            for (i in 0..<nodeList.length) {
                val node = nodeList.item(i)

                val name: String?
                val quantity: Int
                val price: Double
                if (node.nodeType == Node.ELEMENT_NODE) {
                    val eElement = node as Element
                    name = eElement.getAttribute("type")
                    quantity = eElement.getElementsByTagName("Quantity").item(0).textContent.toInt()
                    price = eElement.getElementsByTagName("Price").item(0).textContent.toDouble()

                    foodItemList.add(FoodItem(name, quantity, price))
                }
            }
        } catch (e: ParserConfigurationException) {
            throw RuntimeException(e)
        } catch (e: SAXException) {
            throw RuntimeException(e)
        }


        val order = Order(
            nextOrderNumber, orderType!!, orderDate, foodItemList
        )
        order.company = "GrubStop (XML)"
        return order
    }
}

class SavedJSONParser : Parser {
    override fun parse(file: File) : Order {
        val orderId: Int
        val date: Long
        val totalPrice: Double
        val orderType: Type?
        val orderStatus: Status?
        var originalCompany: String? = null
        val foodItemList: MutableList<FoodItem> = ArrayList<FoodItem>()

        FileReader(file).use { reader ->
            val jsonObject = JSONObject(JSONTokener(reader))
            orderId = jsonObject.getInt("orderID")
            date = jsonObject.getLong("date")
            totalPrice = jsonObject.getDouble("totalPrice")

            val typeStr = jsonObject.getString("type")
            orderType = Type.valueOf(typeStr.uppercase(Locale.getDefault()))

            val statusStr = jsonObject.getString("status")
            orderStatus = Status.valueOf(statusStr)

            if (jsonObject.has("company") && !jsonObject.isNull("company")) {
                originalCompany = jsonObject.getString("company")
            }

            val itemArray = jsonObject.getJSONArray("foodList")
            for (o in itemArray) {
                val item = o as JSONObject
                val quantity = item.getInt("quantity")
                val price = item.getDouble("price")
                val name = item.getString("name")
                foodItemList.add(FoodItem(name, quantity, price))
            }
        }
        val order = Order(orderId, date, totalPrice, orderType!!, orderStatus!!, foodItemList)

        if (originalCompany != null && !originalCompany.isEmpty()) {
            if (originalCompany.startsWith("Restored - ")) {
                order.company = originalCompany
            } else {
                order.company = "Restored - $originalCompany"
            }
        } else {
            order.company = "Restored - Unknown"
        }

        return order
    }
}

/**
 * Main test method for the Parser class.
 * Uses a hardcoded JSON file to test the parser method.
 * Prints to console.
 */
fun main() {

    //test for json file type
    var file = File("src/main/orderFiles/backupOrders/order_09-16-2025_10-00.json")
    var myParser = getParser(file.toString())
    var myOrder = myParser.parse(file)
    println(myOrder)

    //test for xml file type
    file = File("src/main/orderFiles/backupOrders/order_09-16-2025_16-00.xml")
    myParser = getParser(file.toString())
    myOrder = myParser.parse(file)
    println(myOrder)

}

