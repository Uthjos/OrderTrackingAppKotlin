package org.metrostate.ics.ordertrackingappkotlin.ui

import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.layout.*
import javafx.scene.text.Text
import org.metrostate.ics.ordertrackingappkotlin.Directory
import org.metrostate.ics.ordertrackingappkotlin.OrderListener
import org.metrostate.ics.ordertrackingappkotlin.Status
import org.metrostate.ics.ordertrackingappkotlin.Type
import org.metrostate.ics.ordertrackingappkotlin.order.Order
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
    private var selectedOrderBox: VBox? = null
    private var selectedOrder: Order? = null

    private val BASE_BOX_STYLE =
        "-fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-color: #DFE8E8; -fx-cursor: hand;"


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
        statusFilter.onAction = EventHandler { e: ActionEvent? -> populateOrderTiles() }

        //set up type filter box
        typeFilter.getItems().add("All")
        for (t in Type.entries) {
            typeFilter.getItems().add(t.toString())
        }
        typeFilter.value = "All"
        typeFilter.onAction = EventHandler { e: ActionEvent? -> populateOrderTiles() }
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
                    val tile = createOrderTile(order)
                    ordersContainer.children.add(tile)
                }
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

    /**
     * Filters the orders displayed in the GUI based on the selected status and type.
     */
    private fun applyFilters() {
        val selectedStatus = statusFilter.value
        val selectedType = typeFilter.value

        // Clears existing children first to prevent duplicate child errors
        ordersContainer.children.clear()

        // reuse existing boxes when possible to avoid replacing nodes
        val existing: MutableMap<Int?, VBox?> = HashMap<Int?, VBox?>()
        for (node in ordersContainer.children) {
            if (node !is VBox) continue
            val ud = node.userData
            if (ud is Int) {
                existing[ud] = node
            }
        }

        val newChildren: MutableList<Node?> = ArrayList<Node?>()
        // just All for now
        for (order in orders) {
            val statusMatch = selectedStatus == "All" || order.status.toString() == selectedStatus
            val typeMatch = selectedType == "All" || order.type.toString() == selectedType

            if (statusMatch && typeMatch) {
                var box = existing.get(order.orderID)//need explicit here since orderID is an int
                if (box == null) {
                    box = createOrderTile(order) as VBox?
                } else {
                    // update userData just in case and refresh labels
                    box.userData = order.orderID
                    refreshOrderBox(box, order)
                    val finalBox: VBox? = box
                }
                /*box.onMouseClicked = EventHandler { evt: MouseEvent? ->
                    selectOrderBox(finalBox)
                    selectedOrder = order
                    showOrderDetails(order)

                 }*/
                newChildren.add(box)
            }
        }

        ordersContainer.children.setAll(newChildren)

        // re-select the previously selected order if it is still displayed
        val found: VBox? = findOrderBoxForOrder(ordersContainer,selectedOrder)
        if (found != null) selectOrderBox(found)
        //updateButtonsVisibility(selectedOrder)
    }

    /**
     * Finds the left-side VBox for a given order by matching the "Order #<id>" label text.
     *
     * @param order The order to locate
     * @return      The VBox corresponding to the order, or null
    </id> */
    private fun findOrderBoxForOrder(node:Node, order: Order?): VBox? {
        if(node is Pane)
      for (node in node.children) {
          if (node is Label &&  node.text.contains(order?.orderID.toString())) {
              return node.parent as VBox?
          }
          else if (node is Text && node.text.contains(order?.orderID.toString())) {
              return node.parent as VBox?
          }
          else if (node is Pane){
              for (node in node.children) {
                  return findOrderBoxForOrder(node, order)
              }
          }
      }
        return null
    }

    /**
     * Updates the labels inside an orderBox (left-side list) to reflect current order state.
     *
     * @param orderBox  The VBox representing the order
     * @param order     The order whose data will be displayed
     */
    private fun refreshOrderBox(orderBox: VBox, order: Order) {
        val boxToUpdate: VBox = orderBox
        val orderCopy: Order = order
        // do UI update on the JavaFX Application Thread
        Platform.runLater(Runnable {
            try {
                // topRow: [orderTitle, statusLabel]
                if (!boxToUpdate.children.isEmpty()) {
                    if (boxToUpdate.children[0] is HBox) {
                        val topRow = boxToUpdate.children[0] as HBox
                        if (topRow.children.size > 1 && topRow.children[1] is Label) {
                            val statusLabel = topRow.children[1] as Label
                            statusLabel.text = (orderCopy.status.toString())
                            statusLabel.style = "-fx-text-fill: " + orderCopy.status.color + ";"
                        }
                    }

                    // secondRow: [typeLabel, spacer, companyLabel]
                    if (boxToUpdate.children.size > 1 && boxToUpdate.children[1] is HBox) {
                        val secondRow = boxToUpdate.children[1] as HBox
                        if (!secondRow.children.isEmpty() && secondRow.children.first() is Label) {
                            val typeLabel = secondRow.children.first() as Label
                            typeLabel.text = orderCopy.type.toString()
                            typeLabel.style = "-fx-text-fill: " + orderCopy.type.color + "; -fx-font-weight: bold;"
                        }
                    }
                }
                if (boxToUpdate !== selectedOrderBox) {
                    selectOrderBox(boxToUpdate)
                } else {
                    boxToUpdate.style = "$BASE_BOX_STYLE -fx-effect: dropshadow(gaussian, rgba(158,158,158,0.6), 14, 0.5, 0, 0); -fx-border-color: #9e9e9e; -fx-border-width: 1;"
                }
            } catch (e: java.lang.Exception) {
                // let thread die
            }
        })
    }

    /**
     * Visual indicator for selected order box.
     *
     * @param box The VBox representing the order to select
     */
    private fun selectOrderBox(box: VBox?) {
        selectedOrderBox?.style = BASE_BOX_STYLE
        if (box != null) {
            // selected order style around box
            val SELECTED_BOX_STYLE = "$BASE_BOX_STYLE -fx-effect: dropshadow(gaussian, rgba(158,158,158,0.6), 14, 0.5, 0, 0); -fx-border-color: #9e9e9e; -fx-border-width: 1;"
            box.style = SELECTED_BOX_STYLE
            selectedOrderBox = box
        }
    }
}