package org.metrostate.ics.ordertrackingappkotlin

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import org.metrostate.ics.ordertrackingappkotlin.order.Order
import org.metrostate.ics.ordertrackingappkotlin.parser.ParserFactory
import java.io.File
import java.io.IOException

class MainViewController {

    @FXML
    var ordersContainer = VBox()


    val orders: MutableList<Order> = mutableListOf()

    private val parserFactory = ParserFactory()
    private var orderListener: OrderListener? = null

    @FXML
    private fun initialize() {
        // Set up OrderListener to monitor importOrders directory
        val importOrdersPath = Directory.getDirectory(Directory.importOrders)

        orderListener = OrderListener(importOrdersPath, object : OrderListener.OrderFileCallback {
            override fun onNewOrderFile(file: File) {
                handleNewOrder(file)
            }
        })

        // Start listening for new order files
        orderListener?.start()
    }

    /**
     * Called when a new order file is detected in the importOrders directory.
     * Parses the file and adds the order to the list.
     */
    private fun handleNewOrder(file: File) {
        try {
            val parser = parserFactory.getParser(file)
            val order = parser.parse(file)

            orders.add(order)
            populateOrderTiles()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shutdown() {
        orderListener?.stop()
    }

    private fun populateOrderTiles() { // create an order tile for each loaded order
        ordersContainer.children.clear()

        for (order in orders) {
            val tile = createOrderTile(order)
            ordersContainer.children.add(tile)
        }
    }

    private fun createOrderTile(order: Order): Node { // create vbox with order id and status for an order
        val box = VBox()
        box.style = "-fx-border-color: #e0e0e0; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 10; -fx-cursor: hand;"
        box.spacing = 6.0

        val title = Label("Order #${order.orderID}")
        title.style = "-fx-font-weight: bold; -fx-font-size: 14;"

        val statusRow = HBox(8.0)
        statusRow.children.addAll(Label("Status:"), Label(order.status.toString()))
        val spacer = javafx.scene.layout.Region()
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS)

        val companyLabel = Label(order.company ?: "Unknown")
        companyLabel.style = "-fx-text-fill: #666666; -fx-font-size: 11;"

        statusRow.children.addAll(spacer, companyLabel)

        box.children.addAll(title, statusRow)

        box.setOnMouseClicked { _ -> //clicking the order tile will open a different window with order details
            openOrderDetails(order)
        }

        return box
    }

    private fun openOrderDetails(order: Order) { //change the scene root to order details view
        try {
            val loader = FXMLLoader(javaClass.getResource("/org/metrostate/ics/ordertrackingappkotlin/order-details-view.fxml"))
            val detailsRoot = loader.load<Parent>()
            val controller = loader.getController<OrderDetailsController>()
            controller.setOrderDetails(order)

            val scene = ordersContainer.scene
            scene.root = detailsRoot
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}