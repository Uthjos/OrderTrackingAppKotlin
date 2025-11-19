package org.metrostate.ics.ordertrackingappkotlin

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import java.io.IOException

class HelloController {

    @FXML
    var ordersContainer = VBox()

    @FXML
    var orderBoxIdLabel = Label()

    @FXML
    var orderBoxStatusLabel = Label()

    val orders: MutableList<Order> = mutableListOf()

    lateinit var exampleOrder: Order

    @FXML
    private fun initialize() { // grab orders from list. creating one here for now just to have an example order in the gui. later can pull from orderDriver

        exampleOrder = Order(
            1,
            Type.DELIVERY,
            System.currentTimeMillis(),
            mutableListOf(
                FoodItem(name = "Burger", quantity = 2, price = 5.99),
                FoodItem(name = "Fries", quantity = 1, price = 2.99),
                FoodItem(name = "Soda", quantity = 3, price = 1.49)
            )
        )

        orders.add(exampleOrder)

        populateOrderTiles()

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

    @FXML
    @Suppress("unused", "UNUSED_PARAMETER")
    private fun openOrderDetails(_event: MouseEvent) {
        openOrderDetails(exampleOrder)
    }
}