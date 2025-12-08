package org.metrostate.ics.ordertrackingappkotlin

import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import org.metrostate.ics.ordertrackingappkotlin.order.Order

class OrderDetailsController {

    @FXML
    var titleLabel = Label()

    @FXML
    var orderInfo = TextArea()

    fun setOrderDetails(order: Order) {
        titleLabel.text = "Order #${order.orderID}"
        orderInfo.text = order.toString()
    }

    @FXML
    private fun close() { //closing the order details window returns to main view
        // change the scene root back to main view
        try {
            val mainViewRoot = MainApplication.mainViewRoot
            if (mainViewRoot != null) {
                val scene = orderInfo.scene
                scene.root = mainViewRoot
            } else {
                System.err.println("Main view not found")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
