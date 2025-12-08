package org.metrostate.ics.ordertrackingappkotlin.ui

import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.control.Separator
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.layout.*
import org.metrostate.ics.ordertrackingappkotlin.order.*
import org.metrostate.ics.ordertrackingappkotlin.order.Status
import javafx.scene.control.Button
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Controller for the Order Details view.
 * Manages displaying detailed information about a selected order
 * and handling status updates to the order.
 */
class OrderDetailsController {

    @FXML
    var titleLabel = Label()
    @FXML
    var orderDetailsContainer = VBox()
    @FXML
    var cancelButton = Button()
    @FXML
    var startButton = Button()
    @FXML
    var completeButton = Button()
    @FXML
    var resubmitButton = Button()


    private var currentOrder: Order? = null

    fun setOrderDetails(order: Order) {
        currentOrder = order
        titleLabel.text = "Order #${order.orderID}"
        orderDetailsContainer.children.clear()

        // info section
        val infoGrid = GridPane().apply {
            hgap = 20.0
            vgap = 12.0
            style = "-fx-padding: 10 0 0 0;"
        }
        val zdt = Instant.ofEpochMilli(order.date).atZone(ZoneId.of("America/Chicago"))
        val formattedDate = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a z").format(zdt)
        addInfoRow(infoGrid, 1, "Date:", formattedDate)
        addInfoRow(infoGrid, 2, "Type:", order.type.toString(), order.type.color)
        addInfoRow(infoGrid, 3, "Status:", order.status.toString(), order.status.color)
        addInfoRow(infoGrid, 4, "Company:", order.company?.removePrefix("Restored from save- ") ?: "Unknown")
        orderDetailsContainer.children.add(infoGrid)

        orderDetailsContainer.children.add(Separator())

        // foodItems
        val itemsBox = VBox(10.0).apply { style = "-fx-padding: 10 0 0 0;" }
        for (item in order.foodList) {
            val itemBox = HBox(15.0).apply {
                style = "-fx-background-color: #f8f9fa; -fx-padding: 12; -fx-background-radius: 6;"
            }
            itemBox.children.addAll(
                Label("${item.quantity}x").apply {
                    style = "-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #1a1a1a; -fx-min-width: 35;"
                },
                Label(item.name).apply {
                    style = "-fx-font-size: 14; -fx-text-fill: #1a1a1a;"
                    HBox.setHgrow(this, Priority.ALWAYS)
                },
                Label(String.format("$%.2f/ea", item.price)).apply {
                    style = "-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #2e7d32;"
                }
            )
            itemsBox.children.add(itemBox)
        }
        orderDetailsContainer.children.add(itemsBox)

        orderDetailsContainer.children.add(Separator())

        // pricing
        val pricingBox = VBox(8.0).apply { style = "-fx-padding: 10 0 0 0;" }
        addPricingRow(pricingBox, "Subtotal:      ", order.totalPrice, false)
        addPricingRow(pricingBox, "Kitchen Tip:  ", order.getKitchenTip(), false)
        when (order) {
            is DeliveryOrder -> addPricingRow(pricingBox, "Driver Tip:    ", order.getDriverTip(), false)
            is DineInOrder -> addPricingRow(pricingBox, "Server Tip:    ", order.getServerTip(), false)
        }
        pricingBox.children.add(Separator().apply { VBox.setMargin(this, Insets(5.0, 0.0, 5.0, 0.0)) })
        addPricingRow(pricingBox, "Grand Total:  ", order.calculateGrandTotal(), true)
        orderDetailsContainer.children.add(pricingBox)

        updateButtonVisibility()
    }

    private fun updateButtonVisibility() {
        val order = currentOrder ?: return
        startButton.isVisible = order.status == Status.WAITING
        completeButton.isVisible = order.status == Status.IN_PROGRESS
        cancelButton.isVisible = order.status != Status.COMPLETED && order.status != Status.CANCELLED
        resubmitButton.isVisible = order.status == Status.CANCELLED
    }

    @FXML
    @Suppress("UNUSED")
    private fun handleStart() {
        currentOrder?.status = Status.IN_PROGRESS
        currentOrder?.let { setOrderDetails(it) }
    }

    @FXML
    @Suppress("UNUSED")
    private fun handleComplete() {
        currentOrder?.status = Status.COMPLETED
        currentOrder?.let { setOrderDetails(it) }
    }

    @FXML
    @Suppress("UNUSED")
    private fun handleCancel() {
        val order = currentOrder ?: return

        val alert = Alert(Alert.AlertType.ERROR)
        alert.title = "Confirm Cancellation"
        alert.headerText = "Are you sure you want to cancel Order #${order.orderID}?"

        alert.buttonTypes.setAll(ButtonType.YES, ButtonType.NO)
        val result = alert.showAndWait()
        if (result.isPresent && result.get() == ButtonType.YES) {
            order.status = Status.CANCELLED
            setOrderDetails(order)
        }
    }

    @FXML
    @Suppress("UNUSED")
    private fun handleResubmit() {
        currentOrder?.status = Status.WAITING
        currentOrder?.let { setOrderDetails(it) }
    }

    private fun addInfoRow(grid: GridPane, row: Int, label: String, value: String, valueColor: String? = null) {
        val labelNode = Label(label).apply { style = "-fx-font-size: 14; -fx-text-fill: #666666;" }
        val valueNode = Label(value).apply {
            style = "-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: ${valueColor ?: "#1a1a1a"};"
        }
        GridPane.setConstraints(labelNode, 0, row)
        GridPane.setConstraints(valueNode, 1, row)
        grid.children.addAll(labelNode, valueNode)
    }

    private fun addPricingRow(container: VBox, label: String, amount: Double, isTotal: Boolean) {
        val row = HBox().apply {
            style = if (isTotal) "-fx-background-color: #f0f0f0; -fx-padding: 8; -fx-background-radius: 4;" else ""
        }
        row.children.addAll(
            Label(label).apply {
                style = if (isTotal) "-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;"
                else "-fx-font-size: 14; -fx-text-fill: #666666;"
                HBox.setHgrow(this, Priority.ALWAYS)
            },
            Label(String.format("$%.2f", amount)).apply {
                style = if (isTotal) "-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2e7d32;"
                else "-fx-font-size: 14; -fx-text-fill: #1a1a1a;"
            }
        )
        container.children.add(row)
    }

    @FXML
    @Suppress("UNUSED")
    private fun close() {
        try {
            val mainViewRoot = MainApplication.mainViewRoot
            val mainViewController = MainApplication.mainViewController
            if (mainViewRoot != null) {
                // Refresh the main view to show any status changes
                mainViewController?.populateOrderTiles()

                //saveStateonExit here

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
