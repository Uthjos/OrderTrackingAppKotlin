package org.metrostate.ics.ordertrackingappkotlin.ui

import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.layout.*
import org.metrostate.ics.ordertrackingappkotlin.directory.Directory
import org.metrostate.ics.ordertrackingappkotlin.OrderListener
import org.metrostate.ics.ordertrackingappkotlin.directory.SaveState
import org.metrostate.ics.ordertrackingappkotlin.order.Status
import org.metrostate.ics.ordertrackingappkotlin.order.Type
import org.metrostate.ics.ordertrackingappkotlin.order.Order
import org.metrostate.ics.ordertrackingappkotlin.order.OrderDriver
import org.metrostate.ics.ordertrackingappkotlin.parser.ParserFactory
import java.io.File
import java.io.IOException

class MainViewController {

    @FXML
    var ordersContainer = VBox()

    @FXML
    var statusFilter = ComboBox<String>()

    @FXML
    var typeFilter = ComboBox<String>()

    val orders: MutableList<Order> = mutableListOf()

    private val parserFactory = ParserFactory()
    private var orderListener: OrderListener? = null

    private val orderTileBuilder = OrderTileBuilder(this)


    @FXML
    private fun initialize() {
        // Set up OrderListener to monitor importOrders directory
        val importOrdersPath = Directory.getDirectory(Directory.importOrders)

        orderListener = OrderListener(importOrdersPath
        ) { file -> handleNewOrder(file) }

        // Start listening for new order files
        orderListener?.start()
        populateOrderTiles()

        //set up status filter box
        statusFilter.items.add("All")
        for (s in Status.entries) {
            statusFilter.items.add(s.toString())
        }
        statusFilter.value = "All"
        statusFilter.onAction = EventHandler { _: ActionEvent? -> populateOrderTiles() }

        //set up type filter box
        typeFilter.getItems().add("All")
        for (t in Type.entries) {
            typeFilter.getItems().add(t.toString())
        }
        typeFilter.value = "All"
        typeFilter.onAction = EventHandler { _: ActionEvent? -> populateOrderTiles() }
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

        val orderDriver = OrderDriver()
        for (order in orders) {
            orderDriver.addOrder(order)
        }
        SaveState.saveStateOnExit(orderDriver)
    }

    /**
     * Refreshes screen and updates OrderTiles with all orders
     */
    fun populateOrderTiles() { // create an order tile for each loaded order
        ordersContainer.children.clear()

        if (orders.isEmpty()) {
            // no orders imported message
            val noOrdersBox = VBox()
            noOrdersBox.alignment = Pos.CENTER
            noOrdersBox.spacing = 2.0
            noOrdersBox.style =
                "-fx-padding: 80; -fx-background-color: #f8f9fa; -fx-background-radius: 12; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 12;"
            VBox.setMargin(noOrdersBox, Insets(20.0, 20.0, 20.0, 20.0))

            val icon = Label("☹")
            icon.style = "-fx-font-size: 72; -fx-opacity: 0.6;"

            val message1 = Label("No orders found")
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
                val statusMatch = statusFilter.value == "All" || order.status.toString() == statusFilter.value
                val typeMatch = typeFilter.value == "All" || order.type.toString() == typeFilter.value
                if (statusMatch && typeMatch) {

                    val tile = orderTileBuilder.createOrderTile(order)
                    ordersContainer.children.add(tile)
                }
            }
        }
    }

    fun openOrderDetails(order: Order) { //change the scene root to order details view
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