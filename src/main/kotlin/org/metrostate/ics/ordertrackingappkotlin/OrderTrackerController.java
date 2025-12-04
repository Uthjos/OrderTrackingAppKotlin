package org.metrostate.ics.ordertrackingappkotlin;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.metrostate.ics.ordertrackingappkotlin.parser.Parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for the GUI.
 * Manages order display, filtering, and user interactions.
 */
public class OrderTrackerController {
    @FXML
    private VBox ordersContainer;

    @FXML
    private VBox detailContainer;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Button cancelButton;

    @FXML
    private Button undoButton;

    @FXML
    private Button startButton;

    @FXML
    private Button completeButton;

    @FXML
    private Button clearAllOrdersButton;

    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    private ComboBox<String> typeFilter;

    private List<String> orderFiles;
    private OrderListener orderListener;
    private OrderDriver orderDriver;
    private VBox selectedOrderBox = null;
    private Order selectedOrder = null;
    private OrderDriver.OrderChangeListener driverListener = null;

    private final String BASE_BOX_STYLE = "-fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-color: #DFE8E8; -fx-cursor: hand;";

    /**
     * Initializes GUI components.
     * Sets up buttons, filters, and scroll pane defaults.
     */
    @FXML
    public void initialize() {
        this.orderFiles = new ArrayList<>();
        // keep default behavior if scrollPane is present
        // refer to scrollPane to avoid unused-field warning; keep default behavior if null
        if (scrollPane != null) {
            scrollPane.setFitToWidth(true);
        }
        // Cancel buttons are disabled at startup
        if (cancelButton != null){
            cancelButton.setDisable(true);
            cancelButton.setVisible(false);
            // so it doesn't take up space when hidden
            cancelButton.setManaged(false);
        }
        if (undoButton != null){
            undoButton.setDisable(true);
            undoButton.setVisible(false);
            undoButton.setManaged(false);
        }
        if (startButton != null) {
            startButton.setVisible(false);
            startButton.setManaged(false);
            startButton.setOnAction(e -> startSelectedOrder());
        }
        if (completeButton != null) {
            completeButton.setVisible(false);
            completeButton.setManaged(false);
            completeButton.setOnAction(e -> completeSelectedOrder());
        }

        // Connects cancel and undo buttons to their action methods
        if (cancelButton != null) {
            cancelButton.setOnAction(e -> cancelSelectedOrder());
        }
        if (undoButton != null) {
            undoButton.setOnAction(e -> undoCancel());
        }

        if (statusFilter != null) {
            statusFilter.getItems().add("All");
            for (Status s : Status.values()) {
                Order temp = new Order();
                temp.setStatus(s);
                statusFilter.getItems().add(temp.displayStatus());
            }
            statusFilter.setValue("All");
            statusFilter.setOnAction(e -> applyFilters());
        }

        if (typeFilter != null) {
            typeFilter.getItems().add("All");
            for (Type t : Type.values()) {
                typeFilter.getItems().add(t.toString());
            }
            typeFilter.setValue("All");
            typeFilter.setOnAction(e -> applyFilters());
        }

        // clear all orders button hidden at startup
        if (clearAllOrdersButton != null) {
            clearAllOrdersButton.setVisible(false);
            clearAllOrdersButton.setManaged(false);
        }
    }

    /**
     * Sets the listener to notify the GUI of order changes.
     *
     * @param orderListener The listener to notify of order changes
     */
    public void setOrderListener(OrderListener orderListener) {

        this.orderListener = orderListener;
    }

    /**
     * Sets the OrderDriver.
     *
     * @param driver The OrderDriver to set
     */
    public void setOrderDriver(OrderDriver driver) {
        this.orderDriver = driver;
        // register a listener so the controller updates immediately when the model changes
        if (this.orderDriver != null) {
            driverListener = new OrderDriver.OrderChangeListener() {
                @Override
                public void orderAdded(Order order) {
                    // orders on separate thread - update UI on JavaFX thread
                    Platform.runLater(() -> {
                        // save new order to savedOrders
                        OrderDriver.Companion.orderExportJSON(order, Directory.Companion.getDirectory(Directory.savedOrders));
                        applyFilters();
                        updateClearAllButtonVisibility(); //only visible when there are orders
                    });
                }

                @Override
                public void orderChanged(Order order) {
                    //another thread - update UI on JavaFX thread
                    Platform.runLater(() -> {
                        // save updated order to savedOrders
                        OrderDriver.Companion.orderExportJSON(order, Directory.Companion.getDirectory(Directory.savedOrders));
                        applyFilters();
                        VBox box = findOrderBoxForOrder(order);
                        if (box != null) refreshOrderBox(box, order);
                        // if the changed order is the one currently selected, update details/buttons
                        if (selectedOrder != null && order.getOrderID() == selectedOrder.getOrderID()) {
                            selectedOrder = order;
                            showOrderDetails(selectedOrder);
                            updateButtonsVisibility(selectedOrder);
                        }
                    });
                }
            };
            this.orderDriver.addListener(driverListener);
            //update UI again since orders may have been loaded before listener was added
            if (Platform.isFxApplicationThread()) {
                try {
                    applyFilters();
                    updateClearAllButtonVisibility();
                } catch (Exception ignored) { }
            } else {
                Platform.runLater(() -> {
                    applyFilters();
                    updateClearAllButtonVisibility();
                });
            }
         }
    }

    /**
     * Called by the Exit button in the FXML
     */
    @FXML
    private void exitApplication() {
        // stop the OrderListener thread first to avoid holding file handles while we move/delete files
        if (orderListener != null) {
            orderListener.stop();
        }

        saveStateOnExit();

        if (orderDriver != null && driverListener != null) {
            orderDriver.removeListener(driverListener);
        }
        Platform.exit();
        System.exit(0);
    }

    /**
     * Called by the Clear All button in the FXML
     * Clears all orders from the system after confirmation
     */
    @FXML
    private void clearAllOrders() {
        if (orderDriver == null) return;

        // confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear All Orders");
        alert.setHeaderText("Are you sure you want to clear all orders?");
        alert.setContentText("This will remove all orders from the system. This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                orderDriver.clearAllOrders();

                if (ordersContainer != null) {
                    ordersContainer.getChildren().clear();
                }
                if (detailContainer != null) {
                    detailContainer.getChildren().clear();
                    Label header = new Label("Order Details");
                    header.setFont(Font.font("System", FontWeight.BOLD, 16));
                    detailContainer.getChildren().add(header);
                }

                selectedOrder = null;
                selectedOrderBox = null;

                updateClearAllButtonVisibility();
            }
        });
    }

    /**
     * Updates the visibility of the Clear All Orders button based on whether there are any orders.
     */
    private void updateClearAllButtonVisibility() {
        if (clearAllOrdersButton != null && orderDriver != null) {
            boolean hasOrders = orderDriver.getOrderCount() > 0;
            clearAllOrdersButton.setVisible(hasOrders);
            clearAllOrdersButton.setManaged(hasOrders);
        }
    }

    /**
     * Adds a new order file to the display
     * This method is called by the OrderListener when a new file is detected
     */
    public void addOrderFile(File file) {
        if (file == null || ordersContainer == null) {
            return;
        }

        String fileName = file.getName();

        // avoid duplicates
        if (orderFiles.contains(fileName)) {
            return;
        }

        // parse the order on a background thread
        orderFiles.add(fileName);
        new Thread(() -> {
            Order order = null;
            if (fileName.toLowerCase().endsWith(".json")) {
                try {
                    order = Parser.parseJSONOrder(file);
                } catch (IOException e) {
                    // leave null
                }
            }
            if (fileName.toLowerCase().endsWith(".xml")) {
                try {
                    order = Parser.parseXMLOrder(file);
                } catch (IOException e) {
                    // leave null
                }
            }

            final Order fOrder = order;
            Platform.runLater(() -> {
                if (fOrder != null && orderDriver != null) {
                    orderDriver.addOrder(fOrder);
                    applyFilters();
                } else {
                    VBox orderBox = createOrderBox(fileName, null);
                    ordersContainer.getChildren().addFirst(orderBox);
                }
            });
        }).start();
    }

    /**
     * Creates a VBox display for a single order
     * show order id, status, and type
     */
    private VBox createOrderBox(String sourceName, Order order) {
        VBox orderBox = new VBox(6);
        orderBox.setPadding(new Insets(10));
        orderBox.setStyle(BASE_BOX_STYLE);

        // top row: Order #id: + status
        HBox topRow = new HBox(8);
        Label orderTitle = new Label();
        orderTitle.setFont(Font.font("System", FontWeight.BOLD, 14));

        Label statusLabel = new Label();
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 13));

        // second row: type  and company
        HBox secondRow = new HBox(8);
        Label typeLabel = new Label();
        typeLabel.setFont(Font.font("System", 12));
        Label companyLabel = new Label();
        companyLabel.setFont(Font.font("System", 12));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        if (order != null) {
            orderTitle.setText("Order #" + order.getOrderID() + ":");

            // status text and color
            String statusText = order.displayStatus();
            statusLabel.setText(statusText);
            statusLabel.setStyle("-fx-text-fill: " + statusColor(order.getStatus()) + ";");

            // type formatting
            String type = order.displayType();
            typeLabel.setText(type);
            typeLabel.setStyle("-fx-text-fill: " + typeColor(type) + "; -fx-font-weight: bold;");

            companyLabel.setText(order.getCompany());
            // store order id
            orderBox.setUserData(order.getOrderID());
        } else {
            orderTitle.setText(sourceName);
            statusLabel.setText("");
            typeLabel.setText("Parse error");
            typeLabel.setFont(Font.font("System", FontPosture.ITALIC, 12));
            companyLabel.setText("");
        }

        // add details to rows and add rows to the details box on the right
        topRow.getChildren().addAll(orderTitle, statusLabel);
        secondRow.getChildren().addAll(typeLabel, spacer, companyLabel);
        orderBox.getChildren().addAll(topRow, secondRow);

        // click behavior: show details on right pane if parsed
        orderBox.setOnMouseClicked(evt -> {
            selectOrderBox(orderBox);
            selectedOrder = order;
            if (order != null) showOrderDetails(order);
            // hide Cancel button when order is completed or cancelled
            if (cancelButton != null) {
                cancelButton.setDisable(order == null || order.getStatus() == Status.COMPLETED || order.getStatus() == Status.CANCELLED);
            }
            updateButtonsVisibility(order);
        });

        return orderBox;
    }

    /**
     * Updates the visibility and enabled state of action buttons based on the status of the given order.
     *
     * @param order The order whose status determines button states
     */
    private void updateButtonsVisibility(Order order) {
        if (cancelButton != null) {
            // hide the cancel button when order is completed or cancelled
            boolean showCancel = order != null && order.getStatus() != Status.COMPLETED && order.getStatus() != Status.CANCELLED;
            cancelButton.setVisible(showCancel);
            cancelButton.setManaged(showCancel);
            cancelButton.setDisable(order == null || order.getStatus() == Status.COMPLETED || order.getStatus() == Status.CANCELLED);
        }
        if (undoButton != null) {
            // show the Un-cancel button only when the selected order is cancelled
            boolean showUncancel = order != null && order.getStatus() == Status.CANCELLED;
            undoButton.setVisible(showUncancel);
            undoButton.setManaged(showUncancel);
            undoButton.setDisable(!showUncancel);
        }
        if (startButton != null) {
            // only when waiting
            boolean showStart = order != null && order.getStatus() == Status.WAITING;
            startButton.setVisible(showStart);
            startButton.setManaged(showStart);
            startButton.setDisable(!showStart);
        }
        if (completeButton != null) {
            // only when inProgress
            boolean showComplete = order != null && order.getStatus() == Status.IN_PROGRESS;
            completeButton.setVisible(showComplete);
            completeButton.setManaged(showComplete);
            completeButton.setDisable(!showComplete);
        }
    }

    /**
     * Displays the details of a given order in the detail container.
     *
     * @param order The order whose details will be displayed
     */
    private void showOrderDetails(Order order) {
        if (detailContainer == null) return;
        detailContainer.getChildren().clear();

        Label header = new Label("Order Details - #" + order.getOrderID());
        header.setFont(Font.font("System", FontWeight.BOLD, 16));

        TextArea details = new TextArea(order.toString());
        details.setEditable(false);
        details.setWrapText(true);
        details.setPrefWidth(300);
        details.setPrefHeight(400);

        detailContainer.getChildren().addAll(header, details);
    }

    /**
     * Visual indicator for selected order box.
     *
     * @param box The VBox representing the order to select
     */
    private void selectOrderBox(VBox box) {
        if (selectedOrderBox != null) {
            selectedOrderBox.setStyle(BASE_BOX_STYLE);
        }
        if (box != null) {
            // selected order style around box
            String SELECTED_BOX_STYLE = BASE_BOX_STYLE + " -fx-effect: dropshadow(gaussian, rgba(158,158,158,0.6), 14, 0.5, 0, 0); -fx-border-color: #9e9e9e; -fx-border-width: 1;";
            box.setStyle(SELECTED_BOX_STYLE);
            selectedOrderBox = box;
        } else {
            selectedOrderBox = null;
            updateButtonsVisibility(null);
        }
    }


    /**
     * Returns a color code based on the order status.
     *
     * @param status    The status of the order
     * @return          A hex color string corresponding to the status
     */
    private String statusColor(Status status) {
        if (status == null) return "#666666";
        if (status == Status.COMPLETED) { return "#2e7d32"; } // green
        if (status == Status.WAITING) { return "#fb8c00"; } // orange
        if (status == Status.IN_PROGRESS) { return "#1565c0"; } // blue
        if (status == Status.CANCELLED) { return "#c62828"; } // red
        return "#666666";
    }

    /**
     * Returns a color code based on the order type.
     *
     * @param formattedType The formatted order type
     * @return              A hex color string corresponding to the type
     */
    private String typeColor(String formattedType) {
        if (formattedType == null) return "#444444";
        String t = formattedType.toLowerCase();
        if (t.contains("to-go")) return "#6a1b9a"; // purple
        if (t.contains("pickup")) return "#2e7d32"; // green
        if (t.contains("delivery")) return "#1565c0"; // blue
        return "#444444";
    }

    /**
     * Prompts the user to cancel the selected order and updates the GUI.
     */
    private void cancelSelectedOrder() {
        if (selectedOrder == null || orderDriver == null) return;

        // Confirmation message
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel Order");
        alert.setHeaderText("Are you sure you want to cancel this order? Click OK to confirm.");
        alert.setContentText("Order #" + selectedOrder.getOrderID());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (orderDriver.cancelOrderGUI(selectedOrder)) {
                    // rebuild the list so UI reflects the current state
                    Platform.runLater(() -> {
                        applyFilters();
                        showOrderDetails(selectedOrder);
                        // refresh the left-side box for this order
                        VBox found = findOrderBoxForOrder(selectedOrder);
                        if (found != null) refreshOrderBox(found, selectedOrder);
                        updateButtonsVisibility(selectedOrder);

                        Alert info = new Alert(Alert.AlertType.INFORMATION);
                        info.setTitle("Order Cancelled");
                        info.setHeaderText(null);
                        info.setContentText("Order #" + selectedOrder.getOrderID() + " has been cancelled.");
                        info.showAndWait();
                    });
                }
            }
            else {
                // Confirmation message, cancel aborted
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Cancellation Aborted");
                info.setHeaderText(null);
                info.setContentText("Order #" + selectedOrder.getOrderID() + " remains active.");
                info.showAndWait();
            }
        });
    }

    /**
     * Restores a previously cancelled order and updates the GUI.
     */
    private void undoCancel() {
        if (orderDriver == null || selectedOrder == null) return;
        boolean success = orderDriver.uncancelOrder(selectedOrder);
        // refresh UI
        Platform.runLater(() -> {
            if (success) {
                applyFilters();
                VBox found = findOrderBoxForOrder(selectedOrder);
                if (found != null) refreshOrderBox(found, selectedOrder);
                showOrderDetails(selectedOrder);

                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Order Restored");
                info.setHeaderText(null);
                info.setContentText("Order #" + selectedOrder.getOrderID() + " has been restored.");
                info.showAndWait();
            }

            updateButtonsVisibility(selectedOrder);
        });
    }

    /**
     * Starts the selected order (waiting -> inProgress).
     */
    private void startSelectedOrder() {
        if (selectedOrder == null || orderDriver == null) return;
        orderDriver.startOrder(selectedOrder);
        showOrderDetails(selectedOrder);
        updateButtonsVisibility(selectedOrder);

        // replace the corresponding box in the orders list with updated one
        if (ordersContainer != null) {
            VBox oldBox = (selectedOrderBox != null) ? selectedOrderBox : findOrderBoxForOrder(selectedOrder);
            if (oldBox != null) {
                int idx = ordersContainer.getChildren().indexOf(oldBox);
                VBox newBox = createOrderBox("Order #" + selectedOrder.getOrderID(), selectedOrder);
                if (idx >= 0) {
                    ordersContainer.getChildren().set(idx, newBox);
                } else {
                    ordersContainer.getChildren().remove(oldBox);
                    ordersContainer.getChildren().addFirst(newBox);
                }
                selectedOrderBox = newBox;
                selectOrderBox(newBox);
            }
        }
        VBox found = findOrderBoxForOrder(selectedOrder);
        if (found != null) refreshOrderBox(found, selectedOrder);
        Platform.runLater(this::applyFilters);
    }

    /**
     * Completes the selected order (inProgress -> completed)
     */
    private void completeSelectedOrder() {
        if (selectedOrder == null || orderDriver == null) return;
        orderDriver.completeOrder(selectedOrder);
        showOrderDetails(selectedOrder);
        updateButtonsVisibility(selectedOrder);

        // refresh
        if (ordersContainer != null) {
            VBox oldBox = (selectedOrderBox != null) ? selectedOrderBox : findOrderBoxForOrder(selectedOrder);
            if (oldBox != null) {
                int idx = ordersContainer.getChildren().indexOf(oldBox);
                VBox newBox = createOrderBox("Order #" + selectedOrder.getOrderID(), selectedOrder);
                if (idx >= 0) {
                    ordersContainer.getChildren().set(idx, newBox);
                } else {
                    ordersContainer.getChildren().remove(oldBox);
                    ordersContainer.getChildren().addFirst(newBox);
                }
                selectedOrderBox = newBox;
                selectOrderBox(newBox);
            }
        }
        VBox found = findOrderBoxForOrder(selectedOrder);
        if (found != null) refreshOrderBox(found, selectedOrder);
        Platform.runLater(this::applyFilters);
    }

    /**
     * Updates the labels inside an orderBox (left-side list) to reflect current order state.
     *
     * @param orderBox  The VBox representing the order
     * @param order     The order whose data will be displayed
     */
    private void refreshOrderBox(VBox orderBox, Order order) {
        if (order == null) return;
        if (orderBox == null) {
            orderBox = findOrderBoxForOrder(order);
        }
        if (orderBox == null) return;

        final VBox boxToUpdate = orderBox;
        final Order orderCopy = order;
        // do UI update on the JavaFX Application Thread
        Platform.runLater(() -> {
            try {
                // topRow: [orderTitle, statusLabel]
                if (!boxToUpdate.getChildren().isEmpty()) {
                    if (boxToUpdate.getChildren().get(0) instanceof HBox topRow) {
                        if (topRow.getChildren().size() > 1 && topRow.getChildren().get(1) instanceof Label statusLabel) {
                            statusLabel.setText(orderCopy.displayStatus());
                            statusLabel.setStyle("-fx-text-fill: " + statusColor(orderCopy.getStatus()) + ";");
                        }
                    }

                    // secondRow: [typeLabel, spacer, companyLabel]
                    if (boxToUpdate.getChildren().size() > 1 && boxToUpdate.getChildren().get(1) instanceof HBox secondRow) {
                        if (!secondRow.getChildren().isEmpty() && secondRow.getChildren().getFirst() instanceof Label typeLabel) {
                            typeLabel.setText(orderCopy.displayType());
                            typeLabel.setStyle("-fx-text-fill: " + typeColor(orderCopy.displayType()) + "; -fx-font-weight: bold;");
                        }
                    }
                }
                if (boxToUpdate != selectedOrderBox) {
                    selectOrderBox(boxToUpdate);
                } else {
                    boxToUpdate.setStyle(BASE_BOX_STYLE + " -fx-effect: dropshadow(gaussian, rgba(158,158,158,0.6), 14, 0.5, 0, 0); -fx-border-color: #9e9e9e; -fx-border-width: 1;");
                }
            } catch (Exception e) {
                // let thread die
            }
        });
    }

    /**
     * Finds the left-side VBox for a given order by matching the "Order #<id>" label text.
     *
     * @param order The order to locate
     * @return      The VBox corresponding to the order, or null
     */
    private VBox findOrderBoxForOrder(Order order) {
        if (ordersContainer == null || order == null) return null;
        for (javafx.scene.Node node : ordersContainer.getChildren()) {
            if (!(node instanceof VBox vb)) continue;
            if (vb.getChildren().isEmpty()) continue;
            javafx.scene.Node first = vb.getChildren().getFirst();
            if (!(first instanceof HBox topRow)) continue;
            if (topRow.getChildren().isEmpty()) continue;
            javafx.scene.Node labelNode = topRow.getChildren().getFirst();
            if (!(labelNode instanceof Label titleLabel)) continue;
            String txt = titleLabel.getText();
            if (txt == null) continue;
            try {
                int idxHash = txt.indexOf('#');
                int idxColon = txt.indexOf(':');
                if (idxHash >= 0) {
                    String numStr;
                    if (idxColon > idxHash) numStr = txt.substring(idxHash + 1, idxColon).trim();
                    else numStr = txt.substring(idxHash + 1).trim();
                    int id = Integer.parseInt(numStr);
                    if (id == order.getOrderID()) {
                        return vb;
                    }
                }
            } catch (Exception e) {
                // ignore parse errors,continue
            }
        }
        return null;
    }

    /**
     * Filters the orders displayed in the GUI based on the selected status and type.
     */
    private void applyFilters() {
        if (ordersContainer == null || orderDriver == null){
            return;
        }

        String selectedStatus;
        if (statusFilter != null) {
            selectedStatus = statusFilter.getValue();
        } else {
            selectedStatus = "All";
        }

        String selectedType;
        if (typeFilter != null) {
            selectedType = typeFilter.getValue();
        } else {
            selectedType = "All";
        }

        // Clears existing children first to prevent duplicate child errors
        ordersContainer.getChildren().clear();

        // reuse existing boxes when possible to avoid replacing nodes
        Map<Integer, VBox> existing = new HashMap<>();
        for (javafx.scene.Node node : ordersContainer.getChildren()) {
            if (!(node instanceof VBox vb)) continue;
            Object ud = vb.getUserData();
            if (ud instanceof Integer) {
                existing.put((Integer) ud, vb);
            }
        }

        List<javafx.scene.Node> newChildren = new ArrayList<>();
        // just All for now
        for (Order order : orderDriver.getOrders()) {
            boolean statusMatch = selectedStatus.equals("All") ||
                    order.displayStatus().equalsIgnoreCase(selectedStatus);
            boolean typeMatch = selectedType.equals("All") || order.displayType().equalsIgnoreCase(selectedType);

            if (statusMatch && typeMatch) {
                VBox box = existing.get(order.getOrderID());
                if (box == null) {
                    box = createOrderBox("Order #" + order.getOrderID(), order);
                } else {
                    // update userData just in case and refresh labels
                    box.setUserData(order.getOrderID());
                    refreshOrderBox(box, order);
                    VBox finalBox = box;
                    box.setOnMouseClicked(evt -> {
                        selectOrderBox(finalBox);
                        selectedOrder = order;
                        showOrderDetails(order);
                        if (cancelButton != null) {
                            cancelButton.setDisable(order.getStatus() == Status.COMPLETED || order.getStatus() == Status.CANCELLED);
                        }
                        updateButtonsVisibility(order);
                    });
                }
                newChildren.add(box);
            }
        }

        ordersContainer.getChildren().setAll(newChildren);

        // re-select the previously selected order if it is still displayed
        if (selectedOrder != null) {
            VBox found = findOrderBoxForOrder(selectedOrder);
            if (found != null) selectOrderBox(found);
            else selectedOrderBox = null;
            updateButtonsVisibility(selectedOrder);
        }
    }
}
