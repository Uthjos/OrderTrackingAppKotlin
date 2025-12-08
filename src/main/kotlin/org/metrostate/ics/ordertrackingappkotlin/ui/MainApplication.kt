package org.metrostate.ics.ordertrackingappkotlin.ui

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

class MainApplication : Application() {
    private var mainViewController: MainViewController? = null

    companion object {
        var mainViewRoot: Parent? = null
            private set

        var mainViewController: MainViewController? = null
            private set
    }

    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(MainApplication::class.java.getResource("/org/metrostate/ics/ordertrackingappkotlin/main-view.fxml"))
        val root = fxmlLoader.load<Parent>()

        mainViewRoot = root

        val controller = fxmlLoader.getController<MainViewController>()
        this.mainViewController = controller
        MainApplication.mainViewController = controller

        val scene = Scene(root)
        stage.title = "Order Tracker"
        stage.scene = scene

        stage.setOnCloseRequest {
            mainViewController?.shutdown()
        }

        stage.show()
    }
}
