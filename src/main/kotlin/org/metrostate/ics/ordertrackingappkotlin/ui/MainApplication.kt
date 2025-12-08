package org.metrostate.ics.ordertrackingappkotlin.ui

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import org.metrostate.ics.ordertrackingappkotlin.directory.Directory
import org.metrostate.ics.ordertrackingappkotlin.parser.ParserFactory
import java.io.File

/**
 * Main application class for the Order Tracking application.
 * Initializes and launches the JavaFX application.
 */
class MainApplication : Application() {
    private var mainViewController: MainViewController? = null

    companion object {
        var mainViewRoot: Parent? = null
            private set

        var mainViewController: MainViewController? = null
            private set
    }

    override fun start(stage: Stage) {
        // Load any saved orders
        val savedOrderFiles = File(Directory.getDirectory(Directory.savedOrders)).listFiles() ?: arrayOf()

        val restoredOrders = mutableListOf<org.metrostate.ics.ordertrackingappkotlin.order.Order>()
        for (file in savedOrderFiles) {
            if (!file.isFile || file.name.endsWith(".txt", ignoreCase = true)) {
                continue
            }
            try {
                val parser = ParserFactory().getParser(file)
                val order = parser.parse(file)
                restoredOrders.add(order)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        //clear /savedOrders once all orders are restored
        Directory.deleteFilesInDirectory(Directory.savedOrders)

        val fxmlLoader = FXMLLoader(MainApplication::class.java.getResource("/org/metrostate/ics/ordertrackingappkotlin/main-view.fxml"))
        val root = fxmlLoader.load<Parent>()

        mainViewRoot = root

        val controller = fxmlLoader.getController<MainViewController>()
        this.mainViewController = controller
        MainApplication.mainViewController = controller

        controller.orders.addAll(restoredOrders)
        controller.populateOrderTiles()

        val scene = Scene(root)
        stage.title = "Order Tracker"
        stage.scene = scene

        stage.setOnCloseRequest {
            mainViewController?.shutdown()
        }

        stage.show()
    }
}
