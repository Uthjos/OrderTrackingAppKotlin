package org.metrostate.ics.ordertrackingappkotlin;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.metrostate.ics.ordertrackingappkotlin.parser.Parser;
import org.metrostate.ics.ordertrackingappkotlin.parser.ParserFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Main application class for the Order Tracking System.
 * Sets up the JavaFX application and starts the OrderListener to monitor the testOrders directory.
 */
public class OrderTrackerApp extends Application {
    private OrderListener orderListener;
    private OrderDriver driver;

    @Override
    public void start(Stage stage) throws IOException {
        // try to find the FXML file, if we run with coverage it looks through a different path
        // so it has to be found more explicitly
        java.net.URL fxmlUrl = OrderTrackerApp.class.getResource("order-tracker-view.fxml"); //regularly
        if (fxmlUrl == null) { //with coverage
            fxmlUrl = Thread.currentThread().getContextClassLoader()
                    .getResource("org/metrostate/ics/ordertrackingapp/order-tracker-view.fxml");
        }
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);

        OrderTrackerController controller = fxmlLoader.getController();

        driver = new OrderDriver();

        loadSavedOrders();
        controller.setOrderDriver(driver);

        // after saved orders are loaded, watch the importOrders directory
        String importOrdersPath = Directory.Companion.getDirectory(Directory.ImportOrders);
        orderListener = new OrderListener(importOrdersPath, controller::addOrderFile);

        controller.setOrderListener(orderListener);

        orderListener.start();

        stage.setTitle("Order Tracking System");
        stage.setScene(scene);
        stage.show();

        // stop the listener when the application closes and save state
        stage.setOnCloseRequest(event -> {
            if (orderListener != null) {
                orderListener.stop();
            }
            saveCurrentState();
        });
    }

    /**
     * Loads all saved orders from the savedOrders directory on startup.
     * Updates the nextOrderNumber to continue from the highest ID found.
     * Deletes the saved order files after loading.
     */
    private void loadSavedOrders() {
        String projectPath = System.getProperty("user.dir");
        String savedOrdersPath = Paths.get(projectPath, "src", "main", "orderFiles", "savedOrders").toString();
        File savedOrdersDir = new File(savedOrdersPath);

        if (!savedOrdersDir.exists()) {
            if (!savedOrdersDir.mkdirs()) {
                System.err.println("Failed to create saved orders directory: " + savedOrdersPath);
            }
            return;
        }

        File[] savedFiles = savedOrdersDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
        if (savedFiles == null || savedFiles.length == 0) {
            return;
        }

        int maxOrderId = 0;
        List<File> filesToDelete = new ArrayList<>();

        // load each saved order
        for (File file : savedFiles) {
            try {
                ParserFactory parserFactory = new ParserFactory();
                Parser myParser = parserFactory.getParser(file.toString());
                Order order = myParser.parse(file);
                driver.addOrder(order);
                // track maximum order ID from previous state
                if (order.getOrderID() > maxOrderId) {
                    maxOrderId = order.getOrderID();
                }
                filesToDelete.add(file);
            } catch (Exception e) {
                System.err.println("Error loading saved order from " + file.getName() + ": " + e.getMessage());
            }
        }

        if (maxOrderId > 0) {
            Parser.setNextOrderNumber(maxOrderId);
        }

        try { //delay to make sure all file handles are released
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // delete saved orders after loading
        for (File file : filesToDelete) {
            try {
                // force garbabe collection to release file handles for deletion
                System.gc();

                if (file.exists() && !file.delete()) {
                    boolean deleted = new File(file.getAbsolutePath()).delete();
                    if (!deleted) {
                        file.deleteOnExit();
                    }
                }
            } catch (Exception e) {
                file.deleteOnExit();
            }
        }
    }

    /**
     * Saves the current state by exporting all orders to JSON files in the savedOrders directory.
     */
    private void saveCurrentState() {
        String projectPath = System.getProperty("user.dir");
        String savedOrdersPath = Paths.get(projectPath, "src", "main", "orderFiles", "savedOrders").toString();

        File savedOrdersDir = new File(savedOrdersPath);
        if (!savedOrdersDir.exists()) {
            if (!savedOrdersDir.mkdirs()) {
                System.err.println("Failed to create saved orders directory: " + savedOrdersPath);
            }
        }

        if (driver != null) {
            driver.saveAllOrdersToJSON(savedOrdersPath);
        }
    }
}
