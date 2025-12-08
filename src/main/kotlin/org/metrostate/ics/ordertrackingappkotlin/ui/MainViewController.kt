package org.metrostate.ics.ordertrackingappkotlin.ui

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import org.metrostate.ics.ordertrackingappkotlin.Directory
import org.metrostate.ics.ordertrackingappkotlin.OrderListener
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
        val importOrdersPath = Directory.Companion.getDirectory(Directory.importOrders)

        orderListener = OrderListener(importOrdersPath, object : OrderListener.OrderFileCallback {
            override fun onNewOrderFile(file: File) {
                handleNewOrder(file)
            }
        })

        // Start listening for new order files
        orderListener?.start()
        populateOrderTiles()
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

        if (orders.isEmpty()) {
            // no orders imported message
            val noOrdersBox = VBox()
            noOrdersBox.alignment = Pos.CENTER
            noOrdersBox.spacing = 2.0
            noOrdersBox.style = "-fx-padding: 80; -fx-background-color: #f8f9fa; -fx-background-radius: 12; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 12;"
            VBox.setMargin(noOrdersBox, Insets(20.0, 20.0, 20.0, 20.0))

            val icon = Label("☹")
            icon.style = "-fx-font-size: 72; -fx-opacity: 0.6;"

            val message1= Label("No orders found")
            message1.style = "-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #333333;"
            VBox.setMargin(message1, Insets(12.0, 0.0, 8.0, 0.0))

            val message2 = Label("Add valid order file(s) to")
            message2.style = "-fx-font-size: 13; -fx-text-fill: #666666;"

            val path = Label("orderFiles/importOrders")
            path.style = "-fx-font-size: 13; -fx-text-fill: #333333; -fx-font-weight: bold;"

            noOrdersBox.children.addAll(icon, message1, message2, path)
            ordersContainer.children.add(noOrdersBox)
        } else {
            for (order in orders) {
                val tile = createOrderTile(order)
                ordersContainer.children.add(tile)
            }
        }
    }

    private fun createOrderTile(order: Order): Node { // create vbox with order id and status for an order
        val box = VBox()
        box.style = "-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 15; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);"
        box.spacing = 8.0

        // hover effect
        box.setOnMouseEntered {
            box.style = "-fx-background-color: #f8f9fa; -fx-border-color: #1565c0; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 15; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0, 0, 3);"
        }
        box.setOnMouseExited {
            box.style = "-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 15; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);"
        }

        // Order ID on left, type on right colored from enum
        val titleRow = HBox(8.0)
        val title = Label("Order #${order.orderID}")
        title.style = "-fx-font-weight: bold; -fx-font-size: 14;"
        titleRow.children.add(title)

        val titleSpacer = Region()
        HBox.setHgrow(titleSpacer, Priority.ALWAYS)

        val typeLabel = Label(order.type.toString())
        typeLabel.style = "-fx-text-fill: ${order.type.color}; -fx-font-weight: bold; -fx-font-size: 12;"

        titleRow.children.addAll(titleSpacer, typeLabel)

        val statusRow = HBox(8.0)
        statusRow.children.add(Label("Status:"))

        // status on left, colored from enum, company on right
        val statusLabel = Label(order.status.toString())
        statusLabel.style = "-fx-text-fill: ${order.status.color}; -fx-font-weight: bold;"
        statusRow.children.add(statusLabel)

        val spacer = Region()
        HBox.setHgrow(spacer, Priority.ALWAYS)

        val companyLabel = Label(order.company ?: "Unknown")
        companyLabel.style = "-fx-text-fill: #666666; -fx-font-size: 11;"

        statusRow.children.addAll(spacer, companyLabel)

        box.children.addAll(titleRow, statusRow)

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