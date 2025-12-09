package org.metrostate.ics.ordertrackingappkotlin.parser

import org.json.JSONObject
import org.json.JSONTokener
import org.metrostate.ics.ordertrackingappkotlin.order.*
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
    /**
     * Parse a serializable file
     */
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
    }
}

/**
 * Create the appropriate order type based on Type enum.
 */
fun createOrderByType(
    orderId: Int,
    date: Long,
    foodList: MutableList<FoodItem>,
    type: Type,
    kitchenTip: Double = 0.0,
    serverTip: Double = 0.0,
    driverTip: Double = 0.0
): Order {
    val order: Order = when (type) {
        Type.DELIVERY -> DeliveryOrder(orderId, date, foodList)
        Type.DINE_IN -> DineInOrder(orderId, date, foodList)
        Type.TOGO -> ToGoOrder(orderId, date, foodList)
        else -> ToGoOrder(orderId, date, foodList)
    }

    order.setKitchenTip(kitchenTip)
    if (order is DeliveryOrder) {
        order.setDriverTip(driverTip)
    } else if (order is DineInOrder) {
        order.setServerTip(serverTip)
    }

    return order
}

/**
 * Restore an order with status from saved state.
 */
fun restoreOrderByType(
    orderId: Int,
    date: Long,
    totalPrice: Double,
    status: Status,
    foodList: MutableList<FoodItem>,
    type: Type,
    kitchenTip: Double = 0.0,
    serverTip: Double = 0.0,
    driverTip: Double = 0.0
): Order {
    val order: Order = when (type) {
        Type.DELIVERY -> DeliveryOrder(orderId, date, totalPrice, status, foodList, kitchenTip, driverTip)
        Type.DINE_IN -> DineInOrder(orderId, date, totalPrice, status, foodList, kitchenTip, serverTip)
        Type.TOGO -> ToGoOrder(orderId, date, totalPrice, status, foodList, kitchenTip)
        else -> ToGoOrder(orderId, date, totalPrice, status, foodList, kitchenTip)
    }

    return order
}

/**
 * Parser class for imported JSON files
 */
class JSONParser : Parser {
    override fun parse(file: File) : Order {
        val orderDate: Long
        val orderType: Type?
        val foodItemList: MutableList<FoodItem> = ArrayList<FoodItem>()
        var kitchenTip = 0.0
        var serverTip = 0.0
        var driverTip = 0.0

        FileReader(file).use { fr ->
            val jsonObject = JSONObject(JSONTokener(fr))
            val orderJson = jsonObject.getJSONObject("order")
            val orderDateObj = orderJson.get("order_date")
            if (orderDateObj is Number) orderDate = orderDateObj.toLong()
            else orderDate = orderDateObj.toString().toLong()


            val typeStr = orderJson.getString("type")
            orderType = Type.valueOf(typeStr.uppercase(Locale.getDefault()))

            // Read tips if present
            if (orderJson.has("kitchenTip")) kitchenTip = orderJson.getDouble("kitchenTip")
            if (orderJson.has("serverTip")) serverTip = orderJson.getDouble("serverTip")
            if (orderJson.has("driverTip")) driverTip = orderJson.getDouble("driverTip")

            // Nested tip object
            if (orderJson.has("tip")) {
                val tipObj = orderJson.getJSONObject("tip")
                if (tipObj.has("kitchenTip")) kitchenTip = tipObj.getJSONObject("kitchenTip").getDouble("amount")
                if (tipObj.has("serverTip")) serverTip = tipObj.getJSONObject("serverTip").getDouble("amount")
                if (tipObj.has("driverTip")) driverTip = tipObj.getJSONObject("driverTip").getDouble("amount")
            }

            val itemArray = orderJson.getJSONArray("items")
            for (o in itemArray) {
                val item = o as JSONObject
                val quantity = (item.get("quantity") as Number).toInt()
                val price = (item.get("price") as Number).toDouble()
                val name = item.getString("name")
                foodItemList.add(FoodItem(name, quantity, price))
            }
        }
        val order = createOrderByType(
            nextOrderNumber, orderDate, foodItemList, orderType!!,
            kitchenTip, serverTip, driverTip
        )

        order.company = "FoodHub (JSON)"
        return order

    }
}

/**
 * Parser class for XML files
 */
class XMLParser : Parser {
    override fun parse(file: File) : Order {
        var orderDate: Long = 0
        var orderType: Type? = null
        val foodItemList: MutableList<FoodItem> = ArrayList<FoodItem>()
        var kitchenTip = 0.0
        var serverTip = 0.0
        var driverTip = 0.0

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

                    // Reads tips if present
                    val tipNodes = eElement.getElementsByTagName("Tip")
                    if (tipNodes.length > 0) {
                        val tipElement = tipNodes.item(0) as Element
                        val kitchenNode = tipElement.getElementsByTagName("KitchenTip").item(0)
                        val serverNode = tipElement.getElementsByTagName("ServerTip").item(0)
                        val driverNode = tipElement.getElementsByTagName("DriverTip").item(0)
                        if (kitchenNode != null) kitchenTip = kitchenNode.textContent.toDouble()
                        if (serverNode != null) serverTip = serverNode.textContent.toDouble()
                        if (driverNode != null) driverTip = driverNode.textContent.toDouble()
                    }
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


        val order = createOrderByType(
            nextOrderNumber, orderDate, foodItemList, orderType!!,
            kitchenTip, serverTip, driverTip
        )

        order.company = "GrubStop (XML)"
        return order
    }
}

/**
 * Parser class for saved JSON files in program
 */
class SavedJSONParser : Parser {
    override fun parse(file: File) : Order {
        val orderId: Int
        val date: Long
        val totalPrice: Double
        val orderType: Type?
        val orderStatus: Status?
        var originalCompany: String? = null
        val foodItemList: MutableList<FoodItem> = ArrayList<FoodItem>()
        var kitchenTip = 0.0
        var serverTip = 0.0
        var driverTip = 0.0

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

            // Read tips if present
            if (jsonObject.has("tip")) {
                val tipObj = jsonObject.getJSONObject("tip")
                if (tipObj.has("kitchenTip")) kitchenTip = tipObj.getJSONObject("kitchenTip").getDouble("amount")
                if (tipObj.has("serverTip")) serverTip = tipObj.getJSONObject("serverTip").getDouble("amount")
                if (tipObj.has("driverTip")) driverTip = tipObj.getJSONObject("driverTip").getDouble("amount")
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
        val order = restoreOrderByType(
            orderId, date, totalPrice, orderStatus!!, foodItemList, orderType!!,
            kitchenTip, serverTip, driverTip
        )
        // make sure nextOrderNumber is ahead of any restored saved orders
        if (orderId >= nextOrderNumber) {
            nextOrderNumber = orderId + 1
        }

        if (originalCompany != null && !originalCompany.isEmpty()) {
            if (originalCompany.startsWith("Restored from save- ")) {
                order.company = originalCompany
            } else {
                order.company = "Restored from save- $originalCompany"
            }
        } else {
            order.company = "Restored from save"
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
    var file = File("src/test/resources/order_09-16-2025_10-00.json")
    val parserFactory = ParserFactory()
    var myParser = parserFactory.getParser(file)
    var myOrder = myParser.parse(file)
    println(myOrder)

    //test for xml file type
    file = File("src/test/resources/order_09-16-2025_21-00.xml")
    myParser = parserFactory.getParser(file)
    myOrder = myParser.parse(file)
    println(myOrder)

}

