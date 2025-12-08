package org.metrostate.ics.ordertrackingappkotlin.ui

import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import org.metrostate.ics.ordertrackingappkotlin.order.Order

class OrderTileBuilder(val controller: MainViewController) : VBox(){

    fun createOrderTile(order: Order): Node { // create vbox with order id and status for an order
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
            controller.openOrderDetails(order)
        }

        return box
    }
}