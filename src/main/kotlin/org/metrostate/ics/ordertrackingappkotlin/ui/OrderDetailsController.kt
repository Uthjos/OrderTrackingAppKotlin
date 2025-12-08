package org.metrostate.ics.ordertrackingappkotlin.ui

import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.control.Separator
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.metrostate.ics.ordertrackingappkotlin.order.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class OrderDetailsController {

    @FXML
    var titleLabel = Label()

    @FXML
    var orderDetailsContainer = VBox()


    // functions here to handle status update buttons / dynamic button visibility based on current status



    fun setOrderDetails(order: Order) {
        titleLabel.text = "Order #${order.orderID}"

        orderDetailsContainer.children.clear()

        orderDetailsContainer.children.add(createInfoGrid(order))

        orderDetailsContainer.children.add(Separator())

        orderDetailsContainer.children.add(createSectionTitle("Items"))
        orderDetailsContainer.children.add(createItemsList(order))

        orderDetailsContainer.children.add(Separator())

        orderDetailsContainer.children.add(createSectionTitle("Pricing"))
        orderDetailsContainer.children.add(createPricingSection(order))
    }

    private fun createSectionTitle(title: String): Label {
        val label = Label(title)
        label.style = "-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
        return label
    }

    private fun createInfoGrid(order: Order): GridPane {
        val grid = GridPane()
        grid.hgap = 20.0
        grid.vgap = 12.0
        grid.style = "-fx-padding: 10 0 0 0;"

        val zdt = Instant.ofEpochMilli(order.date).atZone(ZoneId.of("America/Chicago"))
        val formattedDate = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a z").format(zdt)
        addInfoRow(grid, 1, "Date:", formattedDate)
        addInfoRow(grid, 2, "Type:", order.type.toString(), order.type.color)
        addInfoRow(grid, 3, "Status:", order.status.toString(), order.status.color)
        addInfoRow(grid, 4, "Company:", order.company!!)
        return grid
    }

    private fun addInfoRow(grid: GridPane, row: Int, label: String, value: String, valueColor: String? = null) {
        val labelNode = Label(label)
        labelNode.style = "-fx-font-size: 14; -fx-text-fill: #666666;"
        GridPane.setConstraints(labelNode, 0, row)

        val valueNode = Label(value)
        val color = valueColor ?: "#1a1a1a"
        valueNode.style = "-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: $color;"
        GridPane.setConstraints(valueNode, 1, row)

        grid.children.addAll(labelNode, valueNode)
    }

    private fun createItemsList(order: Order): VBox {
        val itemsBox = VBox(10.0)
        itemsBox.style = "-fx-padding: 10 0 0 0;"
            for (item in order.foodList) {
                val itemBox = HBox(15.0)
                itemBox.style = "-fx-background-color: #f8f9fa; -fx-padding: 12; -fx-background-radius: 6;"

                val quantityLabel = Label("${item.quantity}x")
                quantityLabel.style = "-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #1a1a1a; -fx-min-width: 35;"

                val nameLabel = Label(item.name)
                nameLabel.style = "-fx-font-size: 14; -fx-text-fill: #1a1a1a;"
                HBox.setHgrow(nameLabel, Priority.ALWAYS)

                val priceLabel = Label(String.format("$%.2f/ea", item.price))
                priceLabel.style = "-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #2e7d32;"

                itemBox.children.addAll(quantityLabel, nameLabel, priceLabel)
                itemsBox.children.add(itemBox)
        }

        return itemsBox
    }

    private fun createPricingSection(order: Order): VBox {
        val pricingBox = VBox(8.0)
        pricingBox.style = "-fx-padding: 10 0 0 0;"

        addPricingRow(pricingBox, "Subtotal:      ", order.totalPrice, false)

        addPricingRow(pricingBox, "Kitchen Tip:  ", order.getKitchenTip(), false)

        when (order) {
            is DeliveryOrder -> addPricingRow(pricingBox, "Driver Tip:    ", order.getDriverTip(), false)
            is DineInOrder -> addPricingRow(pricingBox, "Server Tip:    ", order.getServerTip(), false)
        }

        val separator = Separator()
        VBox.setMargin(separator, Insets(5.0, 0.0, 5.0, 0.0))
        pricingBox.children.add(separator)

        addPricingRow(pricingBox, "Grand Total:  ", order.calculateGrandTotal(), true)

        return pricingBox
    }

    private fun addPricingRow(container: VBox, label: String, amount: Double, isTotal: Boolean) {
        val row = HBox()
        row.style = if (isTotal) "-fx-background-color: #f0f0f0; -fx-padding: 8; -fx-background-radius: 4;" else ""

        val labelNode = Label(label)
        labelNode.style = if (isTotal) {
            "-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;"
        } else {
            "-fx-font-size: 14; -fx-text-fill: #666666;"
        }
        HBox.setHgrow(labelNode, Priority.ALWAYS)

        val valueNode = Label(String.format("$%.2f", amount))
        valueNode.style = if (isTotal) {
            "-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2e7d32;"
        } else {
            "-fx-font-size: 14; -fx-text-fill: #1a1a1a;"
        }

        row.children.addAll(labelNode, valueNode)
        container.children.add(row)
    }

    @FXML
    private fun close() {
        try {
            val mainViewRoot = MainApplication.mainViewRoot
            if (mainViewRoot != null) {
                val scene = orderDetailsContainer.scene
                scene.root = mainViewRoot
            } else {
                System.err.println("Main view not found")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
